package tkdlqh2.schedule_invoice_demo.outbox;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Outbox 이벤트 처리 상태
 */
@Getter
@RequiredArgsConstructor
public enum OutboxEventStatus {
    PENDING("처리 대기 중"),
    PROCESSING("처리 중"),
    COMPLETED("처리 완료"),
    FAILED("처리 실패");

    private final String description;
}