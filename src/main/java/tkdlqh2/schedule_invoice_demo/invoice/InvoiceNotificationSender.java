package tkdlqh2.schedule_invoice_demo.invoice;

/**
 * Invoice 발송 처리 인터페이스
 * <p>
 * 실제 발송 구현체를 주입받아 사용합니다.
 * - Mock 구현체: MockInvoiceNotificationSender
 * - 실제 구현체: 실제 SMS/알림톡 발송 서비스
 */
public interface InvoiceNotificationSender {

    /**
     * Invoice 발송 처리
     *
     * @param invoice 발송할 Invoice
     * @return 발송 성공 여부 (true: 성공, false: 실패)
     */
    boolean send(Invoice invoice);
}