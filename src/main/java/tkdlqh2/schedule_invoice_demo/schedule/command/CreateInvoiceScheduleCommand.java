package tkdlqh2.schedule_invoice_demo.schedule.command;

import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.schedule.IntervalUnit;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleType;

import java.time.LocalDateTime;

/**
 * 청구서 스케줄 등록 명령
 */
public record CreateInvoiceScheduleCommand(
        Corp corp,
        ScheduleType scheduleType,
        LocalDateTime scheduledAt,
        IntervalUnit intervalUnit,
        Integer intervalValue,
        String studentName,
        String guardianPhone,
        Long amount,
        String description
) {
}