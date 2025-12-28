package tkdlqh2.schedule_invoice_demo.invoice;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;
import tkdlqh2.schedule_invoice_demo.corp.Corp;
import tkdlqh2.schedule_invoice_demo.invoice.command.SendInvoiceImmediatelyCommand;

/**
 * 청구서
 */
@Entity
@Table(name = "invoices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corp_id", nullable = false)
    private Corp corp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(nullable = false, length = 50)
    private String studentName;

    @Column(nullable = false, length = 20)
    private String guardianPhone;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 200)
    private String description;

    /**
     * 스케줄로부터 생성된 경우 스케줄 ID (nullable)
     */
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Builder(access = AccessLevel.PROTECTED)
    public Invoice(
            Corp corp,
            String studentName,
            String guardianPhone,
            Long amount,
            String description,
            Long scheduleId
    ) {
        this.corp = corp;
        this.status = InvoiceStatus.PENDING;
        this.studentName = studentName;
        this.guardianPhone = guardianPhone;
        this.amount = amount;
        this.description = description;
        this.scheduleId = scheduleId;
    }

    public static Invoice createForImmediateSend(SendInvoiceImmediatelyCommand command, Corp corp) {
        return new Invoice(corp, command.studentName(), command.guardianPhone(), command.amount(), command.description(), null);
    }

    /**
     * 발송 완료 처리
     */
    public void markAsSent() {
        if (this.status != InvoiceStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 발송 완료 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = InvoiceStatus.SENT;
    }

    /**
     * 발송 실패 처리
     */
    public void markAsFailed() {
        if (this.status != InvoiceStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = InvoiceStatus.FAILED;
    }
}
