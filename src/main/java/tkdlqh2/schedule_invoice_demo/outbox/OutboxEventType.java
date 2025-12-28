package tkdlqh2.schedule_invoice_demo.outbox;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Outbox 이벤트 타입
 */
@Getter
@RequiredArgsConstructor
public enum OutboxEventType {
    INVOICE_SEND_REQUESTED("Invoice 발송 요청");

    private final String description;
}