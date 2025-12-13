package tkdlqh2.schedule_invoice_demo.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스케줄 실행 상태
 */
@Getter
@RequiredArgsConstructor
public enum ScheduleStatus {

    READY("실행 대기 중"),
    PROCESSING("실행 중"),
    COMPLETED("실행 완료"),
    FAILED("실행 실패");

    private final String description;
}
