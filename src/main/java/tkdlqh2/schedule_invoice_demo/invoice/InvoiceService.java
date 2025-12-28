package tkdlqh2.schedule_invoice_demo.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.corp.CorpService;
import tkdlqh2.schedule_invoice_demo.invoice.command.SendInvoiceImmediatelyCommand;
import tkdlqh2.schedule_invoice_demo.invoice.dto.SendInvoiceImmediatelyResponse;
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
    private final InvoiceNotificationSender invoiceNotificationSender;

    /**
     * 즉시 발송 Invoice 생성
     * <p>
     * 처리 순서:
     * 1. Corp 조회
     * 2. Wallet 조회 및 잔액 확인 (락 획득)
     * 3. Invoice 생성 (PENDING)
     * 4. Wallet 차감 + WalletTransaction 생성 (INVOICE_USE)
     * 5. 발송 시도
     * 6-a. 발송 성공: Invoice 상태 변경 (SENT)
     * 6-b. 발송 실패: Invoice 상태 변경 (FAILED) + Wallet 환불
     * 7. 응답 반환
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
        useTransaction = walletTransactionRepository.save(useTransaction);

        // 5. 발송 시도
        boolean sendSuccess = invoiceNotificationSender.send(invoice);

        // 6. 발송 결과에 따른 처리
        if (sendSuccess) {
            // 6-a. 발송 성공: Invoice 상태 변경 (SENT)
            invoice.markAsSent();
        } else {
            // 6-b. 발송 실패: Invoice 상태 변경 (FAILED) + Wallet 환불
            invoice.markAsFailed();

            // Wallet 환불
            wallet.increaseBalance(command.amount());

            // 환불 트랜잭션 생성
            WalletTransaction refundTransaction = WalletTransaction.createInvoiceRefund(
                    wallet,
                    command.amount(),
                    invoice.getId(),
                    useTransaction.getId()  // 원본 사용 트랜잭션 ID
            );
            walletTransactionRepository.save(refundTransaction);
        }

        // 7. 응답 반환
        return SendInvoiceImmediatelyResponse.from(invoice, wallet.getBalance());
    }
}