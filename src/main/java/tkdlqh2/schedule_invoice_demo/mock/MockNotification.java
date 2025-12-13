package tkdlqh2.schedule_invoice_demo.mock;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock 발송 로그
 * 실제 발송 대신 DB에 기록
 */
@Entity
@Table(name = "mock_notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MockNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID corpId;

    @Column(nullable = false, length = 50)
    private String studentName;

    @Column(nullable = false, length = 20)
    private String guardianPhone;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public MockNotification(
            Long invoiceId,
            UUID corpId,
            String studentName,
            String guardianPhone,
            Long amount,
            String description,
            LocalDateTime sentAt
    ) {
        this.invoiceId = invoiceId;
        this.corpId = corpId;
        this.studentName = studentName;
        this.guardianPhone = guardianPhone;
        this.amount = amount;
        this.description = description;
        this.sentAt = sentAt;
    }
}
