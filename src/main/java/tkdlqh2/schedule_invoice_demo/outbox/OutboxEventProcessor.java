package tkdlqh2.schedule_invoice_demo.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox Event Processor
 * <p>
 * 주기적으로 Outbox 이벤트 처리를 스케줄링합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventProcessorService processorService;

    /**
     * Outbox 이벤트 처리 (1초마다 실행)
     */
    @Scheduled(fixedDelay = 1000)
    public void processEvents() {
        processorService.processAllPendingEvents();
    }
}