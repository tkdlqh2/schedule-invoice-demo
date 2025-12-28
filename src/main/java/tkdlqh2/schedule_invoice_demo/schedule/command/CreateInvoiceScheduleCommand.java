package tkdlqh2.schedule_invoice_demo.schedule.command;

import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.schedule.IntervalUnit;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleType;
import tkdlqh2.schedule_invoice_demo.schedule.dto.CreateInvoiceScheduleRequest;

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

    /**
     * Request와 Corp로부터 Command 생성
     */
    public static CreateInvoiceScheduleCommand from(Corp corp, CreateInvoiceScheduleRequest request) {
        return new CreateInvoiceScheduleCommand(
                corp,
                request.scheduleType(),
                request.scheduledAt(),
                request.intervalUnit(),
                request.intervalValue(),
                request.studentName(),
                request.guardianPhone(),
                request.amount(),
                request.description()
        );
    }
}