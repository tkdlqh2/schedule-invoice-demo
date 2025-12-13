package tkdlqh2.schedule_invoice_demo.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 반복 주기 단위
 */
@Getter
@RequiredArgsConstructor
public enum IntervalUnit {

    DAY("일"),
    WEEK("주"),
    MONTH("월"),
    YEAR("년");

    private final String description;

    /**
     * 주어진 시간에 interval만큼 더한 다음 실행 시간 계산
     */
    public LocalDateTime calculateNext(LocalDateTime baseTime, int intervalValue) {
        if (intervalValue <= 0) {
            throw new IllegalArgumentException("intervalValue는 0보다 커야 합니다.");
        }

        return switch (this) {
            case DAY -> baseTime.plusDays(intervalValue);
            case WEEK -> baseTime.plusWeeks(intervalValue);
            case MONTH -> baseTime.plusMonths(intervalValue);
            case YEAR -> baseTime.plusYears(intervalValue);
        };
    }
}
