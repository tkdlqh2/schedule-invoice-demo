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
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleRepository;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleStatus;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DisplayName("DemoSchedulerController E2E 테스트")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
class DemoSchedulerControllerE2ETest extends BaseE2ETest {

    @Autowired
    private InvoiceScheduleRepository scheduleRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("스케줄러 실행 - 스케줄 처리 성공")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void runScheduler_Success() {
        // given
        Map<String, Object> request = Map.of(
                "executeAt", "2025-12-15T10:00:00"
        );

        // when
        given()
                .spec(spec)
                .body(request)
        .when()
                .post("/internal/demo/run-scheduler")
        .then()
                .statusCode(200)
                .body("executeAt", equalTo("2025-12-15T10:00:00"));

        // then - 스케줄이 처리되었는지 확인
        List<InvoiceSchedule> completedSchedules = scheduleRepository.findByStatus(ScheduleStatus.COMPLETED);
        assert !completedSchedules.isEmpty() : "처리된 스케줄이 있어야 합니다.";

        // Invoice가 생성되었는지 확인
        long invoiceCount = invoiceRepository.count();
        assert invoiceCount > 0 : "Invoice가 생성되어야 합니다.";

        // OutboxEvent가 생성되었는지 확인
        List<OutboxEvent> events = outboxEventRepository.findByEventType(OutboxEventType.INVOICE_SEND_REQUESTED);
        assert !events.isEmpty() : "OutboxEvent가 생성되어야 합니다.";
        assert events.stream().allMatch(e -> e.getStatus() == OutboxEventStatus.PENDING)
                : "모든 OutboxEvent 상태가 PENDING이어야 합니다.";
    }

    @Test
    @DisplayName("스케줄러 실행 - executeAt 없이 현재 시각 기준 실행")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void runScheduler_WithoutExecuteAt() {
        // given - request body 없이 실행 (현재 시각 기준)

        // when
        given()
                .spec(spec)
        .when()
                .post("/internal/demo/run-scheduler")
        .then()
                .statusCode(200)
                .body("executeAt", org.hamcrest.Matchers.notNullValue())
                .body("processedCount", greaterThanOrEqualTo(0))
                .body("succeededCount", greaterThanOrEqualTo(0))
                .body("failedCount", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("스케줄러 실행 - executeAt 이후 스케줄은 처리하지 않음")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void runScheduler_OnlyProcessSchedulesBeforeExecuteAt() {
        // given - 2025-12-10 이전의 스케줄만 처리되어야 함
        Map<String, Object> request = Map.of(
                "executeAt", "2025-12-10T23:59:59"
        );

        // when
        given()
                .spec(spec)
                .body(request)
        .when()
                .post("/internal/demo/run-scheduler")
        .then()
                .statusCode(200)
                .body("executeAt", equalTo("2025-12-10T23:59:59"));

        // then - 2025-12-15 이후의 스케줄은 READY 상태로 남아있어야 함
        List<InvoiceSchedule> readySchedules = scheduleRepository.findByStatus(ScheduleStatus.READY);
        assert readySchedules.stream().anyMatch(s -> s.getScheduledAt().isAfter(java.time.LocalDateTime.parse("2025-12-10T23:59:59")))
                : "executeAt 이후의 스케줄은 READY 상태로 남아있어야 합니다.";
    }

    @Test
    @DisplayName("스케줄러 실행 - 잔액 부족으로 일부 스케줄 실패")
    @Sql(scripts = "/sql/test-data-schedule-insufficient-balance.sql", executionPhase = BEFORE_TEST_METHOD)
    void runScheduler_InsufficientBalance() {
        // given - 잔액이 부족한 기관의 스케줄
        Map<String, Object> request = Map.of(
                "executeAt", "2025-12-31T23:59:59"
        );

        // when
        given()
                .spec(spec)
                .body(request)
        .when()
                .post("/internal/demo/run-scheduler")
        .then()
                .statusCode(200)
                .body("executeAt", equalTo("2025-12-31T23:59:59"));

        // then - 실패한 스케줄이 FAILED 상태로 변경되었는지 확인
        List<InvoiceSchedule> failedSchedules = scheduleRepository.findByStatus(ScheduleStatus.FAILED);
        assert !failedSchedules.isEmpty() : "잔액 부족으로 실패한 스케줄이 있어야 합니다.";
    }

    @Test
    @DisplayName("스케줄러 실행 - RECURRING 스케줄의 다음 스케줄 자동 생성")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void runScheduler_RecurringSchedule_NextScheduleCreated() {
        // given
        Long scheduleGroupId = 2L;  // RECURRING 타입 (매월)
        long initialScheduleCount = scheduleRepository.findByScheduleGroupId(scheduleGroupId).size();

        Map<String, Object> request = Map.of(
                "executeAt", "2025-12-16T00:00:00"
        );

        // when
        given()
                .spec(spec)
                .body(request)
        .when()
                .post("/internal/demo/run-scheduler")
        .then()
                .statusCode(200);

        // then - 다음 스케줄이 생성되었는지 확인
        long newScheduleCount = scheduleRepository.findByScheduleGroupId(scheduleGroupId).size();
        assert newScheduleCount > initialScheduleCount
                : String.format("반복 스케줄의 다음 스케줄이 생성되어야 합니다. (초기: %d, 현재: %d)", initialScheduleCount, newScheduleCount);
    }
}