package tkdlqh2.schedule_invoice_demo.schedule.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleRepository;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * READY 상태이고 scheduledAt이 executeAt 이전인 스케줄을 읽는 ItemReader
 */
@Slf4j
@Component
public class InvoiceScheduleItemReader {

    private final InvoiceScheduleRepository scheduleRepository;

    public InvoiceScheduleItemReader(InvoiceScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public RepositoryItemReader<InvoiceSchedule> reader(LocalDateTime executeAt) {
        return new RepositoryItemReaderBuilder<InvoiceSchedule>()
                .name("invoiceScheduleItemReader")
                .repository(scheduleRepository)
                .methodName("findByStatusAndScheduledAtBeforeOrderByScheduledAtAsc")
                .arguments(ScheduleStatus.READY, executeAt)
                .sorts(Map.of("scheduledAt", Sort.Direction.ASC))
                .pageSize(10)
                .build();
    }
}