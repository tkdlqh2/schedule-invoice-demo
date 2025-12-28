package tkdlqh2.schedule_invoice_demo.outbox;

/**
 * Outbox Event Handler 인터페이스
 * <p>
 * 각 EventType별로 구현체를 만들어 이벤트 처리 로직을 캡슐화합니다.
 * 새로운 EventType 추가 시 새로운 Handler만 구현하면 됩니다.
 */
public interface OutboxEventHandler {

    /**
     * 이 Handler가 처리할 수 있는 EventType인지 확인
     *
     * @param eventType 이벤트 타입
     * @return 처리 가능 여부
     */
    boolean supports(OutboxEventType eventType);

    /**
     * 이벤트 처리
     *
     * @param event 처리할 이벤트
     * @throws Exception 처리 실패 시 예외 발생
     */
    void handle(OutboxEvent event) throws Exception;

    /**
     * 보상 트랜잭션 수행 (최대 재시도 초과 시)
     *
     * @param event 실패한 이벤트
     */
    void compensate(OutboxEvent event);
}