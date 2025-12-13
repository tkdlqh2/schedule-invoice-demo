package tkdlqh2.schedule_invoice_demo.invoice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 청구서 상태
 */
@Getter
@RequiredArgsConstructor
public enum InvoiceStatus {

    PENDING("발송 대기 중"),
    SENT("발송 완료"),
    FAILED("발송 실패");

    private final String description;
}
