package tkdlqh2.schedule_invoice_demo.schedule.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceSchedule;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleGroup;
import tkdlqh2.schedule_invoice_demo.schedule.InvoiceScheduleRepository;
import tkdlqh2.schedule_invoice_demo.schedule.ScheduleType;

/**
 * 스케줄 처리 결과를 저장하는 ItemWriter
 * - 성공: COMPLETED 상태로 변경, RECURRING인 경우 다음 스케줄 생성
 * - 실패: FAILED 상태로 변경 (환불은 OutboxEventHandler의 compensate에서 처리)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceScheduleItemWriter implements ItemWriter<ScheduleProcessResult> {

    private final InvoiceScheduleRepository scheduleRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void write(Chunk<? extends ScheduleProcessResult> chunk) {
        for (ScheduleProcessResult result : chunk) {
            InvoiceSchedule schedule = result.getSchedule();

            if (result.isSuccess()) {
                // 성공 처리: COMPLETED 상태로 변경
                schedule.complete(result.getInvoiceId());

                // RECURRING인 경우: 다음 InvoiceSchedule 생성
                InvoiceScheduleGroup group = schedule.getScheduleGroup();
                if (group.getScheduleType() == ScheduleType.RECURRING) {
                    InvoiceSchedule nextSchedule = schedule.createNext();
                    scheduleRepository.save(nextSchedule);
                    log.info("다음 스케줄 생성: scheduleId={}, nextScheduledAt={}",
                            schedule.getId(), nextSchedule.getScheduledAt());
                }

            } else {
                // 실패 처리: FAILED 상태로 변경
                // 환불은 OutboxEventHandler의 compensate에서 처리됨
                schedule.fail(result.getErrorMessage());
            }

            scheduleRepository.save(schedule);
        }
    }
}