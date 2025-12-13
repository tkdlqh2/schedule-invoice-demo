package tkdlqh2.schedule_invoice_demo.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    /**
     * Wallet ID로 트랜잭션 목록 조회 (최신순)
     */
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.id = :walletId ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId);

    /**
     * Invoice ID로 트랜잭션 조회
     */
    List<WalletTransaction> findByInvoiceId(Long invoiceId);

    /**
     * Invoice ID와 Type으로 트랜잭션 조회
     */
    Optional<WalletTransaction> findByInvoiceIdAndType(Long invoiceId, WalletTransactionType type);

    /**
     * Corp ID로 트랜잭션 목록 조회 (최신순)
     */
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.wallet.corp.id = :corpId ORDER BY wt.createdAt DESC")
    List<WalletTransaction> findByCorpIdOrderByCreatedAtDesc(@Param("corpId") UUID corpId);
}
