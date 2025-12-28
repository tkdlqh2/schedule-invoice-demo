package tkdlqh2.schedule_invoice_demo.outbox.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.invoice.Invoice;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceNotificationSender;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceRepository;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEvent;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventHandler;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventType;
import tkdlqh2.schedule_invoice_demo.wallet.Wallet;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransaction;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransactionRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransactionType;

import java.util.List;

/**
 * Invoice 발송 요청 이벤트 처리 Handler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceSendRequestedEventHandler implements OutboxEventHandler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceNotificationSender invoiceNotificationSender;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public boolean supports(OutboxEventType eventType) {
        return eventType == OutboxEventType.INVOICE_SEND_REQUESTED;
    }

    @Override
    public void handle(OutboxEvent event) throws Exception {
        Long invoiceId = event.getAggregateId();

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        // 외부 발송 서비스 호출 (트랜잭션 외부)
        boolean sendSuccess = invoiceNotificationSender.send(invoice);

        if (sendSuccess) {
            // 발송 성공: Invoice 상태 변경
            invoice.markAsSent();
            invoiceRepository.save(invoice);
        } else {
            throw new RuntimeException("Invoice send failed");
        }
    }

    @Override
    @Transactional
    public void compensate(OutboxEvent event) {
        log.warn("Performing compensation for failed invoice send event: {}", event.getId());

        Long invoiceId = event.getAggregateId();

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        // Invoice 실패 처리
        invoice.markAsFailed();
        invoiceRepository.save(invoice);

        // Wallet 환불
        Wallet wallet = walletRepository.findByCorpIdWithLock(invoice.getCorp().getId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        wallet.increaseBalance(invoice.getAmount());

        // 원본 사용 트랜잭션 찾기
        List<WalletTransaction> useTransactions = walletTransactionRepository.findByInvoiceId(invoiceId);
        WalletTransaction useTransaction = useTransactions.stream()
                .filter(t -> t.getType() == WalletTransactionType.INVOICE_USE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("원본 INVOICE_USE 트랜잭션을 찾을 수 없습니다."));

        // 환불 트랜잭션 생성
        WalletTransaction refundTransaction = WalletTransaction.createInvoiceRefund(
                wallet,
                invoice.getAmount(),
                invoiceId,
                useTransaction.getId()
        );
        walletTransactionRepository.save(refundTransaction);

        log.info("Compensation completed for invoice: {}", invoiceId);
    }
}