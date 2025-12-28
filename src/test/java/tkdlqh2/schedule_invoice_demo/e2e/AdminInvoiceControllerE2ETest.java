package tkdlqh2.schedule_invoice_demo.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceRepository;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEvent;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventRepository;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventStatus;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventType;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransactionRepository;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DisplayName("AdminInvoiceController E2E 테스트")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
class AdminInvoiceControllerE2ETest extends BaseE2ETest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Invoice 즉시 발송 - 성공 (Outbox Pattern)")
    @Sql(scripts = "/sql/test-data-invoice.sql", executionPhase = BEFORE_TEST_METHOD)
    void sendInvoiceImmediately_Success() {
        // given - 잔액이 50000원인 기관
        String corpId = "66666666-6666-6666-6666-666666666666";

        Map<String, Object> request = Map.of(
                "studentName", "홍길동",
                "guardianPhone", "010-1234-5678",
                "amount", 10000L,
                "description", "2024년 3월 수업료"
        );

        // when
        Long invoiceId = given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(200)
                .body("invoiceId", notNullValue())
                .body("corpId", equalTo(corpId))
                .body("corpName", equalTo("Invoice 테스트 기관 A"))
                .body("status", equalTo("PENDING"))  // Outbox Pattern: 비동기 처리 대기
                .body("studentName", equalTo("홍길동"))
                .body("guardianPhone", equalTo("010-1234-5678"))
                .body("amount", equalTo(10000))
                .body("description", equalTo("2024년 3월 수업료"))
                .body("remainingWalletBalance", equalTo(40000))  // 50000 - 10000
                .body("createdAt", notNullValue())
                .extract()
                .jsonPath().getLong("invoiceId");

        // then - OutboxEvent가 생성되었는지 확인
        List<OutboxEvent> events = outboxEventRepository.findByAggregateTypeAndAggregateId("INVOICE", invoiceId);
        assert events.size() == 1 : "OutboxEvent가 생성되어야 합니다.";

        OutboxEvent event = events.get(0);
        assert event.getEventType() == OutboxEventType.INVOICE_SEND_REQUESTED : "EventType이 INVOICE_SEND_REQUESTED여야 합니다.";
        assert event.getStatus() == OutboxEventStatus.PENDING : "OutboxEvent 상태가 PENDING이어야 합니다.";
    }

    @Test
    @DisplayName("Invoice 즉시 발송 - 잔액 부족으로 실패")
    @Sql(scripts = "/sql/test-data-invoice.sql", executionPhase = BEFORE_TEST_METHOD)
    void sendInvoiceImmediately_InsufficientBalance() {
        // given - 잔액이 100원인 기관
        String corpId = "77777777-7777-7777-7777-777777777777";

        Map<String, Object> request = Map.of(
                "studentName", "김철수",
                "guardianPhone", "010-9876-5432",
                "amount", 10000L,  // 잔액(100원)보다 큰 금액
                "description", "2024년 3월 수업료"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(500);  // IllegalArgumentException: 잔액 부족
    }

    @Test
    @DisplayName("Invoice 즉시 발송 - Wallet이 없어서 실패")
    @Sql(scripts = "/sql/test-data-invoice.sql", executionPhase = BEFORE_TEST_METHOD)
    void sendInvoiceImmediately_WalletNotFound() {
        // given - Wallet이 없는 기관
        String corpId = "88888888-8888-8888-8888-888888888888";

        Map<String, Object> request = Map.of(
                "studentName", "이영희",
                "guardianPhone", "010-1111-2222",
                "amount", 5000L,
                "description", "2024년 3월 수업료"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(500);  // IllegalArgumentException: Wallet 없음
    }

    @Test
    @DisplayName("Invoice 즉시 발송 - 존재하지 않는 기관")
    void sendInvoiceImmediately_CorpNotFound() {
        // given
        Map<String, Object> request = Map.of(
                "studentName", "박민수",
                "guardianPhone", "010-3333-4444",
                "amount", 5000L,
                "description", "2024년 3월 수업료"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", "00000000-0000-0000-0000-000000000000")
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(500);  // IllegalArgumentException: Corp 없음
    }

    @Test
    @DisplayName("Invoice 즉시 발송 - 유효성 검증 실패 (빈 학생 이름)")
    @Sql(scripts = "/sql/test-data-invoice.sql", executionPhase = BEFORE_TEST_METHOD)
    void sendInvoiceImmediately_ValidationFailed_BlankStudentName() {
        // given
        String corpId = "66666666-6666-6666-6666-666666666666";

        Map<String, Object> request = Map.of(
                "studentName", "",  // 빈 값
                "guardianPhone", "010-1234-5678",
                "amount", 10000L,
                "description", "2024년 3월 수업료"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(400);  // Validation Error
    }

    @Test
    @DisplayName("Invoice 즉시 발송 - 유효성 검증 실패 (0 이하 금액)")
    @Sql(scripts = "/sql/test-data-invoice.sql", executionPhase = BEFORE_TEST_METHOD)
    void sendInvoiceImmediately_ValidationFailed_InvalidAmount() {
        // given
        String corpId = "66666666-6666-6666-6666-666666666666";

        Map<String, Object> request = Map.of(
                "studentName", "홍길동",
                "guardianPhone", "010-1234-5678",
                "amount", 0L,  // 0 이하 금액
                "description", "2024년 3월 수업료"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(400);  // Validation Error
    }

    @Test
    @DisplayName("Invoice 연속 발송 - 잔액 차감 확인")
    @Sql(scripts = "/sql/test-data-invoice.sql", executionPhase = BEFORE_TEST_METHOD)
    void sendInvoiceImmediately_MultipleSends() {
        // given - 잔액이 50000원인 기관
        String corpId = "66666666-6666-6666-6666-666666666666";

        // when - 3번 연속 발송
        sendInvoiceViaAPI(corpId, "학생1", "010-1111-1111", 5000L, "1차 수업료");
        sendInvoiceViaAPI(corpId, "학생2", "010-2222-2222", 10000L, "2차 수업료");
        sendInvoiceViaAPI(corpId, "학생3", "010-3333-3333", 15000L, "3차 수업료");

        // then - 마지막 발송 후 잔액 확인 (50000 - 5000 - 10000 - 15000 = 20000)
        given()
                .pathParam("corpId", corpId)
        .when()
                .get("/admin/corps/{corpId}/wallet")
        .then()
                .statusCode(200)
                .body("balance", equalTo(20000));
    }

    @Test
    @DisplayName("Invoice 발송 후 WalletTransaction 생성 확인")
    @Sql(scripts = "/sql/test-data-invoice.sql", executionPhase = BEFORE_TEST_METHOD)
    void sendInvoiceImmediately_WalletTransactionCreated() {
        // given
        String corpId = "66666666-6666-6666-6666-666666666666";

        Map<String, Object> request = Map.of(
                "studentName", "테스트학생",
                "guardianPhone", "010-8888-8888",
                "amount", 12000L,
                "description", "트랜잭션 테스트"
        );

        // when
        Long invoiceId = given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(200)
                .extract()
                .jsonPath().getLong("invoiceId");

        // then - WalletTransaction이 생성되었는지 확인
        long transactionCount = walletTransactionRepository.findByInvoiceId(invoiceId).size();
        assert transactionCount == 1 : "WalletTransaction (INVOICE_USE)이 생성되어야 합니다.";
    }

    /**
     * 테스트용 Invoice 발송 헬퍼 메서드
     */
    private void sendInvoiceViaAPI(String corpId, String studentName, String guardianPhone, Long amount, String description) {
        Map<String, Object> request = Map.of(
                "studentName", studentName,
                "guardianPhone", guardianPhone,
                "amount", amount,
                "description", description
        );

        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/invoices/send-now")
        .then()
                .statusCode(200);
    }
}