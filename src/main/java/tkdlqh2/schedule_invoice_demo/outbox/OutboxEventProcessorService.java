package tkdlqh2.schedule_invoice_demo.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Outbox Event Processor Service
 * <p>
 * 트랜잭션 처리를 담당하는 서비스
 */
@Slf4j
@Service
public class OutboxEventProcessorService {

    private final OutboxEventRepository outboxEventRepository;
    private final Map<OutboxEventType, OutboxEventHandler> handlerMap;
    private final OutboxEventProcessorService self;

    public OutboxEventProcessorService(
            OutboxEventRepository outboxEventRepository,
            List<OutboxEventHandler> handlers,
            @Lazy OutboxEventProcessorService self
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.self = self;
        this.handlerMap = handlers.stream()
                .flatMap(handler ->
                    Arrays.stream(OutboxEventType.values())
                        .filter(handler::supports)
                        .map(type -> Map.entry(type, handler))
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 모든 PENDING 이벤트를 트랜잭션 내에서 조회
     */
    @Transactional
    public void processAllPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEventsWithLock(OutboxEventStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            try {
                // 각 이벤트를 별도의 트랜잭션으로 처리 (self-invocation 회피)
                self.processEventInNewTransaction(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
            }
        }
    }

    /**
     * 개별 이벤트 처리 (새로운 트랜잭션)
     * <p>
     * REQUIRES_NEW: 항상 새로운 트랜잭션 생성
     * - 각 이벤트 처리가 독립적인 트랜잭션
     * - 한 이벤트 실패가 다른 이벤트에 영향 없음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEventInNewTransaction(OutboxEvent event) {
        log.info("Processing outbox event: {} (type: {}, aggregateId: {})",
                event.getId(), event.getEventType(), event.getAggregateId());

        // Handler 조회
        OutboxEventHandler handler = handlerMap.get(event.getEventType());
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for event type: " + event.getEventType());
        }

        // 처리 시작 표시
        event.startProcessing();
        outboxEventRepository.save(event);

        try {
            // Handler에게 처리 위임
            handler.handle(event);

            // 처리 완료
            event.complete();
            outboxEventRepository.save(event);

            log.info("Successfully processed outbox event: {}", event.getId());

        } catch (Exception e) {
            log.error("Error processing outbox event: {}", event.getId(), e);

            // 처리 실패
            event.fail(e.getMessage());
            outboxEventRepository.save(event);

            // 최대 재시도 초과 시 보상 트랜잭션 수행 (별도 트랜잭션)
            if (!event.canRetry()) {
                try {
                    self.executeCompensation(event, handler);
                } catch (Exception compensationError) {
                    log.error("Compensation failed for event: {}", event.getId(), compensationError);
                }
            }
        }
    }

    /**
     * 보상 트랜잭션 실행 (새로운 트랜잭션)
     * <p>
     * REQUIRES_NEW: 항상 새로운 독립적인 트랜잭션 생성
     * - 보상 트랜잭션 실패가 원래 이벤트 실패 저장에 영향 없음
     * - 원래 트랜잭션은 이미 커밋된 상태
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeCompensation(OutboxEvent event, OutboxEventHandler handler) {
        log.info("Executing compensation for event: {}", event.getId());
        handler.compensate(event);
        log.info("Compensation completed for event: {}", event.getId());
    }
}