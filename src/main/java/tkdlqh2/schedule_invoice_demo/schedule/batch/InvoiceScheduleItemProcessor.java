package tkdlqh2.schedule_invoice_demo.schedule.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.invoice.Invoice;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceRepository;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEvent;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventRepository;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleGroup;
import tkdlqh2.schedule_invoice_demo.wallet.Wallet;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransaction;
import tkdlqh2.schedule_invoice_demo.wallet.WalletTransactionRepository;

import java.util.UUID;

/**
 * 스케줄을 처리하는 ItemProcessor
 * <p>
 * 각 스케줄에 대해:
 * 1. Invoice 생성
 * 2. Wallet 차감 및 트랜잭션 생성
 * 3. OutboxEvent 생성
 * 4. 다음 스케줄 생성 (RECURRING인 경우)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceScheduleItemProcessor implements ItemProcessor<InvoiceSchedule, ScheduleProcessResult> {

    private final InvoiceRepository invoiceRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ScheduleProcessResult process(InvoiceSchedule schedule) {
        log.info("스케줄 처리 시작: scheduleId={}", schedule.getId());

        // 1. 상태 변경: READY -> PROCESSING
        schedule.startProcessing();

        UUID useTransactionId = null;

        try {
            InvoiceScheduleGroup group = schedule.getScheduleGroup();

            // 2. Wallet 조회 및 잔액 확인 (비관적 락)
            Wallet wallet = walletRepository.findByCorpIdWithLock(group.getCorp().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Wallet이 존재하지 않습니다. Corp ID: " + group.getCorp().getId()));

            if (!wallet.hasEnoughBalance(group.getAmount())) {
                throw new IllegalArgumentException(
                        String.format("잔액이 부족합니다. 현재 잔액: %d, 필요 금액: %d",
                                wallet.getBalance(), group.getAmount())
                );
            }

            // 3. Invoice 생성 (PENDING)
            Invoice invoice = Invoice.createFromSchedule(
                    group.getCorp(),
                    group.getStudentName(),
                    group.getGuardianPhone(),
                    group.getAmount(),
                    group.getDescription(),
                    schedule.getId()
            );
            invoice = invoiceRepository.save(invoice);

            // 4. Wallet 차감 + WalletTransaction 생성 (INVOICE_USE)
            wallet.decreaseBalance(group.getAmount());

            WalletTransaction useTransaction = WalletTransaction.createInvoiceUse(
                    wallet,
                    group.getAmount(),
                    invoice.getId()
            );
            useTransaction = walletTransactionRepository.save(useTransaction);
            useTransactionId = useTransaction.getId();

            // 5. OutboxEvent 생성 (발송 요청 이벤트)
            OutboxEvent outboxEvent = OutboxEvent.forInvoiceSendRequest(invoice.getId());
            outboxEventRepository.save(outboxEvent);

            log.info("스케줄 처리 완료: scheduleId={}, invoiceId={}", schedule.getId(), invoice.getId());

            return ScheduleProcessResult.success(schedule, invoice.getId(), useTransactionId);

        } catch (Exception e) {
            log.error("스케줄 처리 실패: scheduleId={}, error={}", schedule.getId(), e.getMessage());
            return ScheduleProcessResult.failure(schedule, useTransactionId, e.getMessage());
        }
    }
}