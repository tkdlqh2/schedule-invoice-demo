package tkdlqh2.schedule_invoice_demo.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * 처리 대기 중인 이벤트 조회 (PESSIMISTIC_WRITE 락)
     * <p>
     * 여러 프로세서가 동시에 같은 이벤트를 처리하지 않도록 락 획득
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEventsWithLock();

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