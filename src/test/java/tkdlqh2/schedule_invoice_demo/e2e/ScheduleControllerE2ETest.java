package tkdlqh2.schedule_invoice_demo.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleGroupRepository;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleRepository;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DisplayName("ScheduleController E2E 테스트")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
class ScheduleControllerE2ETest extends BaseE2ETest {

    @Autowired
    private InvoiceScheduleRepository scheduleRepository;

    @Autowired
    private InvoiceScheduleGroupRepository scheduleGroupRepository;

    @Test
    @DisplayName("청구서 스케줄 등록 - 1회성 스케줄 성공")
    @Sql(scripts = "/sql/test-data-corp.sql", executionPhase = BEFORE_TEST_METHOD)
    void createInvoiceSchedule_Once_Success() {
        // given
        String corpId = "11111111-1111-1111-1111-111111111111";

        Map<String, Object> request = Map.of(
                "scheduleType", "ONCE",
                "scheduledAt", "2025-12-15T10:00:00",
                "studentName", "홍길동",
                "guardianPhone", "010-1234-5678",
                "amount", 50000,
                "description", "2025년 1월 수업료"
        );

        // when
        Long scheduleGroupId = given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/app/corps/{corpId}/invoice-schedules")
        .then()
                .statusCode(201)
                .body("scheduleGroupId", notNullValue())
                .body("corpId", equalTo(corpId))
                .body("scheduleType", equalTo("ONCE"))
                .body("intervalUnit", nullValue())
                .body("intervalValue", nullValue())
                .body("firstScheduleId", notNullValue())
                .body("firstScheduledAt", equalTo("2025-12-15T10:00:00"))
                .body("studentName", equalTo("홍길동"))
                .body("guardianPhone", equalTo("010-1234-5678"))
                .body("amount", equalTo(50000))
                .body("description", equalTo("2025년 1월 수업료"))
                .extract()
                .jsonPath().getLong("scheduleGroupId");

        // then - 스케줄이 1개 생성되었는지 확인
        long scheduleCount = scheduleRepository.findByScheduleGroupIdOrderByScheduledAtAsc(scheduleGroupId).size();
        assert scheduleCount == 1 : "1회성 스케줄은 1개만 생성되어야 합니다.";
    }

    @Test
    @DisplayName("청구서 스케줄 등록 - 반복 스케줄 성공 (매월)")
    @Sql(scripts = "/sql/test-data-corp.sql", executionPhase = BEFORE_TEST_METHOD)
    void createInvoiceSchedule_Recurring_Success() {
        // given
        String corpId = "11111111-1111-1111-1111-111111111111";

        Map<String, Object> request = Map.of(
                "scheduleType", "RECURRING",
                "scheduledAt", "2025-12-15T10:00:00",
                "intervalUnit", "MONTH",
                "intervalValue", 1,
                "studentName", "김철수",
                "guardianPhone", "010-9876-5432",
                "amount", 100000,
                "description", "매월 수업료"
        );

        // when
        Long scheduleGroupId = given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .body(request)
        .when()
                .post("/app/corps/{corpId}/invoice-schedules")
        .then()
                .statusCode(201)
                .body("scheduleGroupId", notNullValue())
                .body("corpId", equalTo(corpId))
                .body("scheduleType", equalTo("RECURRING"))
                .body("intervalUnit", equalTo("MONTH"))
                .body("intervalValue", equalTo(1))
                .body("firstScheduleId", notNullValue())
                .body("firstScheduledAt", equalTo("2025-12-15T10:00:00"))
                .body("studentName", equalTo("김철수"))
                .body("guardianPhone", equalTo("010-9876-5432"))
                .body("amount", equalTo(100000))
                .body("description", equalTo("매월 수업료"))
                .extract()
                .jsonPath().getLong("scheduleGroupId");

        // then - 첫 스케줄이 생성되었는지 확인
        long scheduleCount = scheduleRepository.findByScheduleGroupIdOrderByScheduledAtAsc(scheduleGroupId).size();
        assert scheduleCount == 1 : "첫 번째 스케줄이 생성되어야 합니다.";
    }

    @Test
    @DisplayName("청구서 스케줄 등록 - 존재하지 않는 기관")
    void createInvoiceSchedule_CorpNotFound() {
        // given
        Map<String, Object> request = Map.of(
                "scheduleType", "ONCE",
                "scheduledAt", "2025-12-15T10:00:00",
                "studentName", "홍길동",
                "guardianPhone", "010-1234-5678",
                "amount", 50000,
                "description", "2025년 1월 수업료"
        );

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", "00000000-0000-0000-0000-000000000000")
                .body(request)
        .when()
                .post("/app/corps/{corpId}/invoice-schedules")
        .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("스케줄 그룹 삭제 - 성공 (Soft Delete)")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void deleteScheduleGroup_Success() {
        // given - test-data-schedule.sql에서 생성된 스케줄 그룹
        String corpId = "11111111-1111-1111-1111-111111111111";
        Long scheduleGroupId = 1L;

        // when
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .pathParam("scheduleGroupId", scheduleGroupId)
        .when()
                .delete("/app/corps/{corpId}/invoice-schedules/{scheduleGroupId}")
        .then()
                .statusCode(204);

        // then - Soft Delete 확인: @Where 필터에 의해 조회되지 않음
        boolean groupExists = scheduleGroupRepository.findById(scheduleGroupId).isPresent();
        assert !groupExists : "Soft Delete된 스케줄 그룹은 조회되지 않아야 합니다.";

        // 관련 스케줄은 그대로 유지됨 (이력 보존)
        long scheduleCount = scheduleRepository.findByScheduleGroupIdOrderByScheduledAtAsc(scheduleGroupId).size();
        assert scheduleCount > 0 : "Soft Delete 후에도 스케줄은 유지되어야 합니다.";
    }

    @Test
    @DisplayName("스케줄 그룹 삭제 - 존재하지 않는 기관")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void deleteScheduleGroup_CorpNotFound() {
        // given
        Long scheduleGroupId = 1L;

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", "00000000-0000-0000-0000-000000000000")
                .pathParam("scheduleGroupId", scheduleGroupId)
        .when()
                .delete("/app/corps/{corpId}/invoice-schedules/{scheduleGroupId}")
        .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("스케줄 그룹 삭제 - 존재하지 않는 스케줄 그룹")
    @Sql(scripts = "/sql/test-data-corp.sql", executionPhase = BEFORE_TEST_METHOD)
    void deleteScheduleGroup_ScheduleGroupNotFound() {
        // given
        String corpId = "11111111-1111-1111-1111-111111111111";
        Long nonExistentScheduleGroupId = 99999L;

        // when & then
        given()
                .spec(spec)
                .pathParam("corpId", corpId)
                .pathParam("scheduleGroupId", nonExistentScheduleGroupId)
        .when()
                .delete("/app/corps/{corpId}/invoice-schedules/{scheduleGroupId}")
        .then()
                .statusCode(500);
    }
}