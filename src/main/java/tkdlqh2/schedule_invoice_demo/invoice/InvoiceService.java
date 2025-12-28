package tkdlqh2.schedule_invoice_demo.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.corp.CorpService;
import tkdlqh2.schedule_invoice_demo.invoice.command.SendInvoiceImmediatelyCommand;
import tkdlqh2.schedule_invoice_demo.invoice.dto.SendInvoiceImmediatelyResponse;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEvent;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventRepository;
import tkdlqh2.schedule_invoice_demo.wallet.Wallet;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransaction;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransactionRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CorpService corpService;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final OutboxEventRepository outboxEventRepository;

    /**
     * 즉시 발송 Invoice 생성 (Outbox Pattern 적용)
     * <p>
     * 처리 순서:
     * 1. Corp 조회
     * 2. Wallet 조회 및 잔액 확인 (락 획득)
     * 3. Invoice 생성 (PENDING)
     * 4. Wallet 차감 + WalletTransaction 생성 (INVOICE_USE)
     * 5. OutboxEvent 생성 (발송 요청 이벤트)
     * 6. 트랜잭션 커밋
     * 7. 응답 반환
     * <p>
     * 실제 외부 발송은 OutboxEventProcessor가 비동기로 처리합니다.
     */
    @Transactional
    public SendInvoiceImmediatelyResponse sendInvoiceImmediately(SendInvoiceImmediatelyCommand command) {
        // 1. Corp 조회
        Corp corp = corpService.findCorpById(command.corpId());

        // 2. Wallet 조회 및 잔액 확인 (비관적 락)
        Wallet wallet = walletRepository.findByCorpIdWithLock(command.corpId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet이 존재하지 않습니다. Corp ID: " + command.corpId()));

        if (!wallet.hasEnoughBalance(command.amount())) {
            throw new IllegalArgumentException(
                    String.format("잔액이 부족합니다. 현재 잔액: %d, 필요 금액: %d",
                            wallet.getBalance(), command.amount())
            );
        }

        // 3. Invoice 생성 (PENDING)
        Invoice invoice = Invoice.createForImmediateSend(command, corp);
        invoice = invoiceRepository.save(invoice);

        // 4. Wallet 차감 + WalletTransaction 생성 (INVOICE_USE)
        wallet.decreaseBalance(command.amount());

        WalletTransaction useTransaction = WalletTransaction.createInvoiceUse(
                wallet,
                command.amount(),
                invoice.getId()
        );
        walletTransactionRepository.save(useTransaction);

        // 5. OutboxEvent 생성 (발송 요청 이벤트)
        // 실제 외부 발송은 OutboxEventProcessor가 비동기로 처리
        OutboxEvent outboxEvent = OutboxEvent.forInvoiceSendRequest(invoice.getId());
        outboxEventRepository.save(outboxEvent);

        // 6. 응답 반환 (Invoice는 PENDING 상태로 반환)
        return SendInvoiceImmediatelyResponse.from(invoice, wallet.getBalance());
    }
}