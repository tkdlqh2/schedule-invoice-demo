package tkdlqh2.schedule_invoice_demo.schedule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceScheduleRepository extends JpaRepository<InvoiceSchedule, Long> {

    /**
     * ScheduleGroup ID로 스케줄 목록 조회 (스케줄 시간 순)
     */
    @Query("SELECT s FROM InvoiceSchedule s WHERE s.scheduleGroup.id = :scheduleGroupId ORDER BY s.scheduledAt ASC")
    List<InvoiceSchedule> findByScheduleGroupIdOrderByScheduledAtAsc(@Param("scheduleGroupId") Long scheduleGroupId);

    /**
     * 실행 대기 중인 스케줄 조회 (스케줄 시간이 executeAt 이하인 READY 상태)
     */
    @Query("SELECT s FROM InvoiceSchedule s WHERE s.status = :status AND s.scheduledAt <= :executeAt ORDER BY s.scheduledAt ASC")
    List<InvoiceSchedule> findByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
            @Param("status") ScheduleStatus status,
            @Param("executeAt") LocalDateTime executeAt
    );

    /**
     * 실행 대기 중인 스케줄 조회 (페이징) - Spring Batch용
     * scheduleGroup과 corp를 JOIN FETCH하여 함께 로드
     */
    @Query("SELECT s FROM InvoiceSchedule s JOIN FETCH s.scheduleGroup sg JOIN FETCH sg.corp WHERE s.status = :status AND s.scheduledAt <= :executeAt")
    Page<InvoiceSchedule> findByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
            @Param("status") ScheduleStatus status,
            @Param("executeAt") LocalDateTime executeAt,
            Pageable pageable
    );

    /**
     * 실행 대기 중인 스케줄 조회 (비관적 락)
     * 동시성 제어를 위한 잠금
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSchedule s WHERE s.status = :status AND s.scheduledAt <= :executeAt ORDER BY s.scheduledAt ASC")
    List<InvoiceSchedule> findByStatusAndScheduledAtBeforeWithLock(
            @Param("status") ScheduleStatus status,
            @Param("executeAt") LocalDateTime executeAt
    );

    /**
     * ScheduleGroup ID와 Status로 스케줄 목록 조회
     */
    @Query("SELECT s FROM InvoiceSchedule s WHERE s.scheduleGroup.id = :scheduleGroupId AND s.status = :status ORDER BY s.scheduledAt ASC")
    List<InvoiceSchedule> findByScheduleGroupIdAndStatus(
            @Param("scheduleGroupId") Long scheduleGroupId,
            @Param("status") ScheduleStatus status
    );

    /**
     * Status로 스케줄 목록 조회
     */
    List<InvoiceSchedule> findByStatus(ScheduleStatus status);

    /**
     * ScheduleGroup ID로 스케줄 목록 조회
     */
    @Query("SELECT s FROM InvoiceSchedule s WHERE s.scheduleGroup.id = :scheduleGroupId")
    List<InvoiceSchedule> findByScheduleGroupId(@Param("scheduleGroupId") Long scheduleGroupId);
}
