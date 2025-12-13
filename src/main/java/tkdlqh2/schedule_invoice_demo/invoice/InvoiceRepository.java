package tkdlqh2.schedule_invoice_demo.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Corp ID로 청구서 목록 조회 (최신순)
     */
    @Query("SELECT i FROM Invoice i WHERE i.corp.id = :corpId ORDER BY i.createdAt DESC")
    List<Invoice> findByCorpIdOrderByCreatedAtDesc(@Param("corpId") UUID corpId);

    /**
     * Corp ID와 Status로 청구서 목록 조회
     */
    @Query("SELECT i FROM Invoice i WHERE i.corp.id = :corpId AND i.status = :status ORDER BY i.createdAt DESC")
    List<Invoice> findByCorpIdAndStatus(@Param("corpId") UUID corpId, @Param("status") InvoiceStatus status);

    /**
     * Schedule ID로 청구서 목록 조회
     */
    List<Invoice> findByScheduleId(Long scheduleId);

    /**
     * Status로 청구서 목록 조회
     */
    List<Invoice> findByStatus(InvoiceStatus status);
}
