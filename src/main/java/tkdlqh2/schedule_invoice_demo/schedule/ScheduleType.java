package tkdlqh2.schedule_invoice_demo.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스케줄 타입
 */
@Getter
@RequiredArgsConstructor
public enum ScheduleType {

    ONCE("1회 실행"),
    RECURRING("반복 실행");

    private final String description;
}
