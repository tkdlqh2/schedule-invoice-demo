package tkdlqh2.schedule_invoice_demo.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import tkdlqh2.schedule_invoice_demo.corp.CorpRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DisplayName("AdminWalletController E2E 테스트")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
class AdminWalletControllerE2ETest extends BaseE2ETest {

    @Autowired
    private CorpRepository corpRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @DisplayName("무료 포인트 지급 - Wallet이 없으면 자동 생성")
    void chargeFreePoints_CreateWallet() {
        // given
        String corpId = createCorpViaAPI("테스트 기관", "123-45-67890", "010-1234-5678");

        Map<String, Object> request = Map.of(
                "amount", 10000L,
                "reason", "신규 가입 축하 포인트"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/wallet/free-charge")
        .then()
                .statusCode(200)
                .body("walletId", notNullValue())
                .body("corpId", equalTo(corpId))
                .body("corpName", equalTo("테스트 기관"))
                .body("chargedAmount", equalTo(10000))
                .body("currentBalance", equalTo(10000))
                .body("reason", equalTo("신규 가입 축하 포인트"));
    }

    @Test
    @DisplayName("무료 포인트 지급 - 기존 Wallet에 추가")
    void chargeFreePoints_ExistingWallet() {
        // given
        String corpId = createCorpViaAPI("테스트 기관", "123-45-67890", "010-1234-5678");

        // 첫 번째 포인트 지급
        chargeFreePointsViaAPI(corpId, 5000L, "첫 번째 지급");

        Map<String, Object> request = Map.of(
                "amount", 3000L,
                "reason", "두 번째 지급"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/wallet/free-charge")
        .then()
                .statusCode(200)
                .body("chargedAmount", equalTo(3000))
                .body("currentBalance", equalTo(8000));  // 5000 + 3000
    }

    @Test
    @DisplayName("무료 포인트 지급 - 0 이하 금액은 실패")
    void chargeFreePoints_InvalidAmount() {
        // given
        String corpId = createCorpViaAPI("테스트 기관", "123-45-67890", "010-1234-5678");

        Map<String, Object> request = Map.of(
                "amount", 0L,
                "reason", "잘못된 금액"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/wallet/free-charge")
        .then()
                .statusCode(500);  // IllegalArgumentException
    }

    @Test
    @DisplayName("Wallet 조회 - 존재하는 경우")
    @Sql(scripts = "/sql/test-data-wallet.sql", executionPhase = BEFORE_TEST_METHOD)
    void getWallet_Exists() {
        // given - SQL로 삽입된 데이터 사용
        String corpId = "44444444-4444-4444-4444-444444444444";

        // when & then
        given()
                .pathParam("corpId", corpId)
        .when()
                .get("/admin/corps/{corpId}/wallet")
        .then()
                .statusCode(200)
                .body("walletId", equalTo(100))
                .body("corpId", equalTo(corpId))
                .body("corpName", equalTo("지갑 테스트 기관 A"))
                .body("balance", equalTo(10000));
    }

    @Test
    @DisplayName("Wallet 조회 - 존재하지 않는 경우 실패")
    @Sql(scripts = "/sql/test-data-wallet.sql", executionPhase = BEFORE_TEST_METHOD)
    void getWallet_NotExists() {
        // given - SQL로 삽입된 Wallet이 없는 기관
        String corpId = "55555555-5555-5555-5555-555555555555";

        // when & then (Wallet을 생성하지 않음)
        given()
                .pathParam("corpId", corpId)
        .when()
                .get("/admin/corps/{corpId}/wallet")
        .then()
                .statusCode(500);  // IllegalArgumentException
    }

    @Test
    @DisplayName("존재하지 않는 기관에 포인트 지급 시도")
    void chargeFreePoints_CorpNotFound() {
        // given
        Map<String, Object> request = Map.of(
                "amount", 10000L,
                "reason", "테스트"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", "00000000-0000-0000-0000-000000000000")
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/wallet/free-charge")
        .then()
                .statusCode(500);  // IllegalArgumentException
    }

    @Test
    @DisplayName("연속으로 여러 번 포인트 지급")
    @Sql(scripts = "/sql/test-data-wallet.sql", executionPhase = BEFORE_TEST_METHOD)
    void chargeFreePoints_Multiple() {
        // given - 기존 잔액 10000원이 있는 기관
        String corpId = "44444444-4444-4444-4444-444444444444";

        // when - 3번 지급
        chargeFreePointsViaAPI(corpId, 1000L, "첫 번째");
        chargeFreePointsViaAPI(corpId, 2000L, "두 번째");
        chargeFreePointsViaAPI(corpId, 3000L, "세 번째");

        // then - 최종 잔액 확인 (10000 + 1000 + 2000 + 3000)
        given()
                .pathParam("corpId", corpId)
        .when()
                .get("/admin/corps/{corpId}/wallet")
        .then()
                .statusCode(200)
                .body("balance", equalTo(16000));  // 10000 + 1000 + 2000 + 3000
    }

    /**
     * 테스트용 기관 생성 헬퍼 메서드
     */
    private String createCorpViaAPI(String name, String businessNumber, String contactPhone) {
        Map<String, Object> request = Map.of(
                "name", name,
                "businessNumber", businessNumber,
                "contactPhone", contactPhone
        );

        return given()
                .spec(spec)
                .body(request)
        .when()
                .post("/admin/corps")
        .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    /**
     * 테스트용 무료 포인트 지급 헬퍼 메서드
     */
    private void chargeFreePointsViaAPI(String corpId, Long amount, String reason) {
        Map<String, Object> request = Map.of(
                "amount", amount,
                "reason", reason
        );

        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/admin/corps/{corpId}/wallet/free-charge")
        .then()
                .statusCode(200);
    }
}