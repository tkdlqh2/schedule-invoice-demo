package tkdlqh2.schedule_invoice_demo.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;
import tkdlqh2.schedule_invoice_demo.schedule.dto.RunSchedulerResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Invoice Scheduler 서비스
 * Spring Batch Job을 실행하여 예약된 스케줄을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceSchedulerService {

    private final JobLauncher jobLauncher;
    private final Job processSchedulesJob;

    /**
     * 스케줄러 실행 (데모용)
     * executeAt 시점까지 실행되어야 하는 모든 스케줄을 처리합니다.
     */
    public RunSchedulerResponse runScheduler(LocalDateTime executeAt) {
        log.info("스케줄러 실행 시작: executeAt={}", executeAt);

        try {
            // Job Parameters 설정
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executeAt", executeAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .addLong("timestamp", System.currentTimeMillis()) // 매번 다른 파라미터로 실행되도록
                    .toJobParameters();

            // Batch Job 실행
            JobExecution jobExecution = jobLauncher.run(processSchedulesJob, jobParameters);

            // 실행 결과 수집
            int processedCount = 0;
            int succeededCount = 0;
            int failedCount = 0;

            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                processedCount += stepExecution.getReadCount();
                succeededCount += stepExecution.getWriteCount();
                failedCount += stepExecution.getRollbackCount();
            }

            log.info("스케줄러 실행 완료: 처리={}, 성공={}, 실패={}", processedCount, succeededCount, failedCount);

            return new RunSchedulerResponse(executeAt, processedCount, succeededCount, failedCount);

        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("스케줄러 실행 실패: error={}", e.getMessage(), e);
            throw new RuntimeException("스케줄러 실행 중 오류가 발생했습니다.", e);
        }
    }
}