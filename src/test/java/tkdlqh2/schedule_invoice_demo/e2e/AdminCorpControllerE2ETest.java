package tkdlqh2.schedule_invoice_demo.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import tkdlqh2.schedule_invoice_demo.corp.CorpRepository;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DisplayName("AdminCorpController E2E 테스트")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
class AdminCorpControllerE2ETest extends BaseE2ETest {

    @Autowired
    private CorpRepository corpRepository;

    @Test
    @DisplayName("기관 생성 성공")
    void createCorp_Success() {
        // given
        Map<String, Object> request = Map.of(
                "name", "테스트 기관",
                "businessNumber", "123-45-67890",
                "contactPhone", "010-1234-5678"
        );

        // when & then
        given()
                .spec(spec)
                .body(request)
        .when()
                .post("/admin/corps")
        .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo("테스트 기관"))
                .body("businessNumber", equalTo("123-45-67890"))
                .body("contactPhone", equalTo("010-1234-5678"));
    }

    @Test
    @DisplayName("기관 생성 - 사업자 번호 중복 시 실패")
    void createCorp_DuplicateBusinessNumber_Fail() {
        // given
        Map<String, Object> firstRequest = Map.of(
                "name", "첫 번째 기관",
                "businessNumber", "123-45-67890",
                "contactPhone", "010-1111-1111"
        );

        given()
                .spec(spec)
                .body(firstRequest)
        .when()
                .post("/admin/corps")
        .then()
                .statusCode(200);

        Map<String, Object> duplicateRequest = Map.of(
                "name", "두 번째 기관",
                "businessNumber", "123-45-67890",  // 중복
                "contactPhone", "010-2222-2222"
        );

        // when & then
        given()
                .spec(spec)
                .body(duplicateRequest)
        .when()
                .post("/admin/corps")
        .then()
                .statusCode(500);  // IllegalArgumentException이 500으로 매핑됨
    }

    @Test
    @DisplayName("기관 목록 조회")
    @Sql(scripts = "/sql/test-data-corp.sql", executionPhase = BEFORE_TEST_METHOD)
    void listCorps() {
        // when & then - 3개의 기본 데이터가 있어야 함
        given()
        .when()
                .get("/admin/corps")
        .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].name", oneOf("테스트 기관 A", "테스트 기관 B", "테스트 기관 C"))
                .body("[1].name", oneOf("테스트 기관 A", "테스트 기관 B", "테스트 기관 C"))
                .body("[2].name", oneOf("테스트 기관 A", "테스트 기관 B", "테스트 기관 C"))
                .body("[0].businessNumber", notNullValue())
                .body("[1].businessNumber", notNullValue())
                .body("[2].businessNumber", notNullValue());
    }

    @Test
    @DisplayName("기관 상세 조회")
    @Sql(scripts = "/sql/test-data-corp.sql", executionPhase = BEFORE_TEST_METHOD)
    void getCorp() {
        // given - SQL로 삽입된 데이터 사용
        String corpId = "11111111-1111-1111-1111-111111111111";

        // when & then
        given()
                .pathParam("corpId", corpId)
        .when()
                .get("/admin/corps/{corpId}")
        .then()
                .statusCode(200)
                .body("id", equalTo(corpId))
                .body("name", equalTo("테스트 기관 A"))
                .body("businessNumber", equalTo("111-11-11111"))
                .body("contactPhone", equalTo("010-1111-1111"));
    }

    @Test
    @DisplayName("존재하지 않는 기관 조회 시 실패")
    void getCorp_NotFound() {
        // when & then
        given()
                .pathParam("corpId", "00000000-0000-0000-0000-000000000000")
        .when()
                .get("/admin/corps/{corpId}")
        .then()
                .statusCode(500);  // IllegalArgumentException이 500으로 매핑됨
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
}