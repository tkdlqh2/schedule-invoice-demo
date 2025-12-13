package tkdlqh2.schedule_invoice_demo.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceScheduleGroupRepository extends JpaRepository<InvoiceScheduleGroup, Long> {

    /**
     * Corp ID로 스케줄 그룹 목록 조회 (최신순)
     */
    @Query("SELECT isg FROM InvoiceScheduleGroup isg WHERE isg.corp.id = :corpId ORDER BY isg.createdAt DESC")
    List<InvoiceScheduleGroup> findByCorpIdOrderByCreatedAtDesc(@Param("corpId") UUID corpId);

    /**
     * Corp ID와 ScheduleType으로 스케줄 그룹 목록 조회
     */
    @Query("SELECT isg FROM InvoiceScheduleGroup isg WHERE isg.corp.id = :corpId AND isg.scheduleType = :scheduleType ORDER BY isg.createdAt DESC")
    List<InvoiceScheduleGroup> findByCorpIdAndScheduleType(
            @Param("corpId") UUID corpId,
            @Param("scheduleType") ScheduleType scheduleType
    );
}
