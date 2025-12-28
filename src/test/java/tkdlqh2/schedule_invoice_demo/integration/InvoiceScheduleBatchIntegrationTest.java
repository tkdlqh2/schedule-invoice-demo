package tkdlqh2.schedule_invoice_demo.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import tkdlqh2.schedule_invoice_demo.invoice.Invoice;
import tkdlqh2.schedule_invoice_demo.invoice.InvoiceRepository;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEvent;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventRepository;
import tkdlqh2.schedule_invoice_demo.outbox.OutboxEventType;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleRepository;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleStatus;
import tkdlqh2.schedule_invoice_demo.wallet.Wallet;
import tkdlqh2.schedule_invoice_demo.wallet.WalletRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBatchTest
@DisplayName("InvoiceScheduleBatch Integration 테스트")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
class InvoiceScheduleBatchIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job processSchedulesJob;

    @Autowired
    private InvoiceScheduleRepository scheduleRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Batch Job 실행 - 스케줄 처리 성공")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void processSchedulesJob_Success() throws Exception {
        // given
        LocalDateTime executeAt = LocalDateTime.parse("2025-12-15T10:00:00");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executeAt", executeAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        // 스케줄이 처리되었는지 확인
        List<InvoiceSchedule> completedSchedules = scheduleRepository.findByStatus(ScheduleStatus.COMPLETED);
        assertFalse(completedSchedules.isEmpty(), "처리된 스케줄이 있어야 합니다.");

        // Invoice가 생성되었는지 확인
        List<Invoice> invoices = invoiceRepository.findAll();
        assertFalse(invoices.isEmpty(), "Invoice가 생성되어야 합니다.");

        // OutboxEvent가 생성되었는지 확인
        List<OutboxEvent> events = outboxEventRepository.findByEventType(OutboxEventType.INVOICE_SEND_REQUESTED);
        assertFalse(events.isEmpty(), "OutboxEvent가 생성되어야 합니다.");
    }

    @Test
    @DisplayName("Batch Job 실행 - Wallet 잔액 차감 확인")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void processSchedulesJob_WalletBalanceDecrease() throws Exception {
        // given
        String corpId = "11111111-1111-1111-1111-111111111111";
        Wallet walletBefore = walletRepository.findByCorpId(java.util.UUID.fromString(corpId))
                .orElseThrow();
        long initialBalance = walletBefore.getBalance();

        LocalDateTime executeAt = LocalDateTime.parse("2025-12-15T10:00:00");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executeAt", executeAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        Wallet walletAfter = walletRepository.findByCorpId(java.util.UUID.fromString(corpId))
                .orElseThrow();
        assertTrue(walletAfter.getBalance() < initialBalance,
                "Wallet 잔액이 차감되어야 합니다.");
    }

    @Test
    @DisplayName("Batch Job 실행 - 잔액 부족 시 스케줄 실패 처리")
    @Sql(scripts = "/sql/test-data-schedule-insufficient-balance.sql", executionPhase = BEFORE_TEST_METHOD)
    void processSchedulesJob_InsufficientBalance() throws Exception {
        // given
        LocalDateTime executeAt = LocalDateTime.parse("2025-12-31T23:59:59");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executeAt", executeAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus(),
                "Job은 완료되어야 합니다 (일부 아이템 실패는 허용)");

        // 실패한 스케줄이 FAILED 상태로 변경되었는지 확인
        List<InvoiceSchedule> failedSchedules = scheduleRepository.findByStatus(ScheduleStatus.FAILED);
        assertFalse(failedSchedules.isEmpty(), "잔액 부족으로 실패한 스케줄이 있어야 합니다.");

        // 실패 메시지 확인
        InvoiceSchedule failedSchedule = failedSchedules.get(0);
        assertNotNull(failedSchedule.getFailureReason(), "실패 메시지가 있어야 합니다.");
        assertTrue(failedSchedule.getFailureReason().contains("잔액이 부족합니다"),
                "실패 메시지에 잔액 부족 내용이 포함되어야 합니다.");
    }

    @Test
    @DisplayName("Batch Job 실행 - RECURRING 스케줄의 다음 스케줄 자동 생성")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void processSchedulesJob_RecurringSchedule_NextScheduleCreated() throws Exception {
        // given
        Long scheduleGroupId = 3L;  // RECURRING 타입 (매월)
        long initialScheduleCount = scheduleRepository.findByScheduleGroupId(scheduleGroupId).size();

        LocalDateTime executeAt = LocalDateTime.parse("2025-12-16T00:00:00");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executeAt", executeAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        // 다음 스케줄이 생성되었는지 확인
        long newScheduleCount = scheduleRepository.findByScheduleGroupId(scheduleGroupId).size();
        assertTrue(newScheduleCount > initialScheduleCount,
                String.format("반복 스케줄의 다음 스케줄이 생성되어야 합니다. (초기: %d, 현재: %d)", initialScheduleCount, newScheduleCount));

        // 다음 스케줄의 scheduledAt 확인
        List<InvoiceSchedule> schedules = scheduleRepository.findByScheduleGroupId(scheduleGroupId);
        InvoiceSchedule nextSchedule = schedules.stream()
                .filter(s -> s.getStatus() == ScheduleStatus.READY)
                .filter(s -> s.getScheduledAt().isAfter(executeAt))
                .findFirst()
                .orElseThrow(() -> new AssertionError("다음 스케줄이 생성되어야 합니다."));

        assertEquals(LocalDateTime.parse("2025-12-17T09:00"), nextSchedule.getScheduledAt(),
                "다음 스케줄의 실행 시각이 이전 스케줄로부터 1주 후여야 합니다.");
    }

    @Test
    @DisplayName("Batch Step 실행 - 청크 단위 처리 확인")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void processSchedulesStep_ChunkProcessing() throws Exception {
        // given
        LocalDateTime executeAt = LocalDateTime.parse("2026-12-31T23:59:59");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executeAt", executeAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertEquals("processSchedulesStep", stepExecution.getStepName());
        assertTrue(stepExecution.getReadCount() > 0, "읽은 아이템이 있어야 합니다.");
        assertTrue(stepExecution.getWriteCount() > 0, "쓴 아이템이 있어야 합니다.");
        assertEquals(stepExecution.getReadCount(), stepExecution.getWriteCount(),
                "읽은 아이템 수와 쓴 아이템 수가 같아야 합니다.");
    }

    @Test
    @DisplayName("Batch Job 실행 - executeAt 이후 스케줄은 처리하지 않음")
    @Sql(scripts = "/sql/test-data-schedule.sql", executionPhase = BEFORE_TEST_METHOD)
    void processSchedulesJob_OnlyProcessSchedulesBeforeExecuteAt() throws Exception {
        // given - 2025-12-10 이전의 스케줄만 처리되어야 함
        LocalDateTime executeAt = LocalDateTime.parse("2025-12-10T23:59:59");
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executeAt", executeAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        // 2025-12-10 이후의 스케줄은 READY 상태로 남아있어야 함
        List<InvoiceSchedule> readySchedules = scheduleRepository.findByStatus(ScheduleStatus.READY);
        assertTrue(readySchedules.stream()
                        .anyMatch(s -> s.getScheduledAt().isAfter(executeAt)),
                "executeAt 이후의 스케줄은 READY 상태로 남아있어야 합니다.");
    }
}