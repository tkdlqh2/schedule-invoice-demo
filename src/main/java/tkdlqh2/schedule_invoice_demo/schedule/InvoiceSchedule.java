package tkdlqh2.schedule_invoice_demo.schedule;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;

import java.time.LocalDateTime;

/**
 * 청구서 스케줄 (실행 단위)
 */
@Entity
@Table(name = "invoice_schedules", indexes = {
        @Index(name = "idx_status_scheduled_at", columnList = "status, scheduled_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvoiceSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_group_id", nullable = false)
    private InvoiceScheduleGroup scheduleGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    /**
     * 실제 실행된 시간 (nullable)
     */
    @Column
    private LocalDateTime executedAt;

    /**
     * 생성된 청구서 ID (nullable)
     */
    @Column(name = "invoice_id")
    private Long invoiceId;

    /**
     * 실패 사유 (nullable)
     */
    @Column(length = 500)
    private String failureReason;

    @Builder(access = AccessLevel.PRIVATE)
    public InvoiceSchedule(
            InvoiceScheduleGroup scheduleGroup,
            LocalDateTime scheduledAt
    ) {
        this.scheduleGroup = scheduleGroup;
        this.status = ScheduleStatus.READY;
        this.scheduledAt = scheduledAt;
    }

    /**
     * 첫 번째 스케줄 생성 (Factory Method)
     */
    public static InvoiceSchedule createFirst(InvoiceScheduleGroup scheduleGroup, LocalDateTime scheduledAt) {
        return InvoiceSchedule.builder()
                .scheduleGroup(scheduleGroup)
                .scheduledAt(scheduledAt)
                .build();
    }

    /**
     * 실행 시작 (READY -> PROCESSING)
     */
    public void startProcessing() {
        if (this.status != ScheduleStatus.READY) {
            throw new IllegalStateException("READY 상태에서만 실행을 시작할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ScheduleStatus.PROCESSING;
        this.executedAt = LocalDateTime.now();
    }

    /**
     * 실행 완료 (PROCESSING -> COMPLETED)
     */
    public void complete(Long invoiceId) {
        if (this.status != ScheduleStatus.PROCESSING) {
            throw new IllegalStateException("PROCESSING 상태에서만 완료 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ScheduleStatus.COMPLETED;
        this.invoiceId = invoiceId;
    }

    /**
     * 실행 실패 (PROCESSING -> FAILED)
     */
    public void fail(String failureReason) {
        if (this.status != ScheduleStatus.PROCESSING) {
            throw new IllegalStateException("PROCESSING 상태에서만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ScheduleStatus.FAILED;
        this.failureReason = failureReason;
    }

    /**
     * 다음 스케줄 생성 (RECURRING인 경우)
     */
    public InvoiceSchedule createNext() {
        if (this.scheduleGroup.getScheduleType() != ScheduleType.RECURRING) {
            throw new IllegalStateException("RECURRING 타입만 다음 스케줄을 생성할 수 있습니다.");
        }

        LocalDateTime nextScheduledAt = this.scheduleGroup.calculateNextScheduledAt(this.scheduledAt);

        return InvoiceSchedule.builder()
                .scheduleGroup(this.scheduleGroup)
                .scheduledAt(nextScheduledAt)
                .build();
    }
}
