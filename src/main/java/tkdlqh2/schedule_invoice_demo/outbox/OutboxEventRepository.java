package tkdlqh2.schedule_invoice_demo.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * 처리 대기 중인 이벤트 조회 (PESSIMISTIC_WRITE 락 + SKIP LOCKED + LIMIT)
     * <p>
     * 여러 인스턴스가 동시에 실행될 때:
     * - 이미 다른 인스턴스가 락을 건 이벤트는 건너뜀 (SKIP LOCKED)
     * - 락이 걸리지 않은 이벤트만 가져와서 처리
     * - 한 번에 최대 10개까지만 가져와서 메모리 효율적
     * - 각 인스턴스가 서로 다른 이벤트를 처리하여 효율적
     */
    @Query(value = "SELECT * FROM outbox_events WHERE status = :status ORDER BY created_at ASC FOR UPDATE SKIP LOCKED LIMIT 10", nativeQuery = true)
    List<OutboxEvent> findPendingEventsWithLock(@Param("status") OutboxEventStatus status);

    /**
     * 특정 타입의 이벤트 조회
     */
    List<OutboxEvent> findByEventType(OutboxEventType eventType);

    /**
     * 특정 상태의 이벤트 조회
     */
    List<OutboxEvent> findByStatus(OutboxEventStatus status);

    /**
     * Aggregate ID로 이벤트 조회
     */
    List<OutboxEvent> findByAggregateTypeAndAggregateId(String aggregateType, Long aggregateId);
}