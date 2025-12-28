package tkdlqh2.schedule_invoice_demo.schedule;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.schedule.command.CreateInvoiceScheduleCommand;

import java.time.LocalDateTime;

/**
 * 청구서 스케줄 그룹 (템플릿 정보 포함)
 * 주의: 수정(UPDATE) 불가, 삭제 및 생성만 가능
 */
@Entity
@Table(name = "invoice_schedule_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
public class InvoiceScheduleGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corp_id", nullable = false)
    private Corp corp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleType scheduleType;

    /**
     * RECURRING인 경우에만 사용
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private IntervalUnit intervalUnit;

    /**
     * RECURRING인 경우에만 사용
     */
    @Column
    private Integer intervalValue;

    // 청구서 템플릿 정보
    @Column(nullable = false, length = 50)
    private String studentName;

    @Column(nullable = false, length = 20)
    private String guardianPhone;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 200)
    private String description;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder(access = AccessLevel.PRIVATE)
    public InvoiceScheduleGroup(
            Corp corp,
            ScheduleType scheduleType,
            IntervalUnit intervalUnit,
            Integer intervalValue,
            String studentName,
            String guardianPhone,
            Long amount,
            String description
    ) {
        this.corp = corp;
        this.scheduleType = scheduleType;
        this.intervalUnit = intervalUnit;
        this.intervalValue = intervalValue;
        this.studentName = studentName;
        this.guardianPhone = guardianPhone;
        this.amount = amount;
        this.description = description;

        // RECURRING인 경우 interval 필수 검증
        if (scheduleType == ScheduleType.RECURRING) {
            if (intervalUnit == null || intervalValue == null || intervalValue <= 0) {
                throw new IllegalArgumentException("RECURRING 타입은 intervalUnit과 intervalValue(양수)가 필수입니다.");
            }
        }
    }

    /**
     * Command로부터 InvoiceScheduleGroup 생성 (Factory Method)
     */
    public static InvoiceScheduleGroup from(CreateInvoiceScheduleCommand command) {
        return InvoiceScheduleGroup.builder()
                .corp(command.corp())
                .scheduleType(command.scheduleType())
                .intervalUnit(command.intervalUnit())
                .intervalValue(command.intervalValue())
                .studentName(command.studentName())
                .guardianPhone(command.guardianPhone())
                .amount(command.amount())
                .description(command.description())
                .build();
    }

    /**
     * 다음 실행 시간 계산 (RECURRING인 경우에만 사용)
     */
    public LocalDateTime calculateNextScheduledAt(LocalDateTime currentScheduledAt) {
        if (scheduleType != ScheduleType.RECURRING) {
            throw new IllegalStateException("ONCE 타입은 다음 실행 시간을 계산할 수 없습니다.");
        }
        return intervalUnit.calculateNext(currentScheduledAt, intervalValue);
    }

    /**
     * Soft Delete 수행
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
