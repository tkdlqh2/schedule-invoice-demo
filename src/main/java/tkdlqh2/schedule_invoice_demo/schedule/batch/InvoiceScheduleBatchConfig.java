package tkdlqh2.schedule_invoice_demo.schedule.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Invoice Schedule Batch 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class InvoiceScheduleBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InvoiceScheduleItemReader itemReaderFactory;
    private final InvoiceScheduleItemProcessor itemProcessor;
    private final InvoiceScheduleItemWriter itemWriter;

    @Bean
    public Job processSchedulesJob() {
        return new JobBuilder("processSchedulesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(processSchedulesStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step processSchedulesStep(@Value("#{jobParameters['executeAt']}") String executeAtStr) {
        LocalDateTime executeAt = executeAtStr != null
                ? LocalDateTime.parse(executeAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : LocalDateTime.now();

        return new StepBuilder("processSchedulesStep", jobRepository)
                .<InvoiceSchedule, ScheduleProcessResult>chunk(10, transactionManager)
                .reader(itemReaderFactory.reader(executeAt))
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }
}