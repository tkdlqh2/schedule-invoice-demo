package tkdlqh2.schedule_invoice_demo.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tkdlqh2.schedule_invoice_demo.common.BaseTimeEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbox Event 엔티티
 * <p>
 * 트랜잭션 외부 작업(외부 API 호출 등)을 비동기로 처리하기 위한 이벤트 저장소
 * Transactional Outbox Pattern 구현
 */
@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    /**
     * 이벤트 타입 (예: INVOICE_SEND_REQUESTED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OutboxEventType eventType;

    /**
     * 집합(Aggregate) 타입 (예: INVOICE)
     */
    @Column(nullable = false, length = 50)
    private String aggregateType;

    /**
     * 집합 ID (예: Invoice ID)
     */
    @Column(nullable = false)
    private Long aggregateId;

    /**
     * 이벤트 페이로드 (JSON 형식)
     */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /**
     * 처리 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    /**
     * 재시도 횟수
     */
    @Column(nullable = false)
    private Integer retryCount = 0;

    /**
     * 최대 재시도 횟수
     */
    @Column(nullable = false)
    private Integer maxRetryCount = 3;

    /**
     * 처리 완료 시각
     */
    private LocalDateTime processedAt;

    /**
     * 에러 메시지
     */
    @Column(length = 1000)
    private String errorMessage;

    @Builder
    public OutboxEvent(
            OutboxEventType eventType,
            String aggregateType,
            Long aggregateId,
            String payload,
            Integer maxRetryCount
    ) {
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxEventStatus.PENDING;
        this.retryCount = 0;
        this.maxRetryCount = maxRetryCount != null ? maxRetryCount : 3;
    }

    /**
     * 처리 시작
     */
    public void startProcessing() {
        if (this.status != OutboxEventStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 처리를 시작할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = OutboxEventStatus.PROCESSING;
    }

    /**
     * 처리 완료
     */
    public void complete() {
        this.status = OutboxEventStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 처리 실패 (재시도 가능)
     */
    public void fail(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;

        if (this.retryCount >= this.maxRetryCount) {
            this.status = OutboxEventStatus.FAILED;
            this.processedAt = LocalDateTime.now();
        } else {
            this.status = OutboxEventStatus.PENDING;  // 재시도 대기
        }
    }

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }
}