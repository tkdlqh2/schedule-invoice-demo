package tkdlqh2.schedule_invoice_demo.schedule.batch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;

import java.util.UUID;

/**
 * 스케줄 처리 결과
 */
@Getter
@RequiredArgsConstructor
public class ScheduleProcessResult {

    private final InvoiceSchedule schedule;
    private final boolean success;
    private final Long invoiceId;
    private final UUID useTransactionId;
    private final String errorMessage;

    public static ScheduleProcessResult success(InvoiceSchedule schedule, Long invoiceId, UUID useTransactionId) {
        return new ScheduleProcessResult(schedule, true, invoiceId, useTransactionId, null);
    }

    public static ScheduleProcessResult failure(InvoiceSchedule schedule, UUID useTransactionId, String errorMessage) {
        return new ScheduleProcessResult(schedule, false, null, useTransactionId, errorMessage);
    }
}