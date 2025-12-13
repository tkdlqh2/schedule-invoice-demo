package tkdlqh2.schedule_invoice_demo.mock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MockNotificationRepository extends JpaRepository<MockNotification, Long> {

    /**
     * Corp ID로 발송 로그 목록 조회 (최신순)
     */
    @Query("SELECT m FROM MockNotification m WHERE m.corpId = :corpId ORDER BY m.sentAt DESC")
    List<MockNotification> findByCorpIdOrderBySentAtDesc(@Param("corpId") UUID corpId);

    /**
     * Invoice ID로 발송 로그 조회
     */
    List<MockNotification> findByInvoiceId(Long invoiceId);

    /**
     * 모든 발송 로그 조회 (최신순)
     */
    @Query("SELECT m FROM MockNotification m ORDER BY m.sentAt DESC")
    List<MockNotification> findAllOrderBySentAtDesc();
}
