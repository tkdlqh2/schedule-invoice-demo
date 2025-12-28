package tkdlqh2.schedule_invoice_demo.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public OutboxEventProcessorService(
            OutboxEventRepository outboxEventRepository,
            List<OutboxEventHandler> handlers
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.handlerMap = handlers.stream()
                .flatMap(handler ->
                    Arrays.stream(OutboxEventType.values())
                        .filter(handler::supports)
                        .map(type -> Map.entry(type, handler))
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 모든 PENDING 이벤트를 트랜잭션 내에서 조회 및 처리
     */
    @Transactional
    public void processAllPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEventsWithLock(OutboxEventStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
            }
        }
    }

    /**
     * 개별 이벤트 처리
     */
    private void processEvent(OutboxEvent event) {
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

            // 최대 재시도 초과 시 보상 트랜잭션 수행
            if (!event.canRetry()) {
                handler.compensate(event);
            }
        }
    }
}