package tkdlqh2.schedule_invoice_demo.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tkdlqh2.schedule_invoice_demo.corp.Corp;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Corp으로 Wallet 조회
     */
    Optional<Wallet> findByCorp(Corp corp);

    /**
     * Corp ID로 Wallet 조회
     */
    @Query("SELECT w FROM Wallet w WHERE w.corp.id = :corpId")
    Optional<Wallet> findByCorpId(@Param("corpId") UUID corpId);

    /**
     * Corp ID로 Wallet 조회 (비관적 락)
     * 동시성 제어를 위한 잠금
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.corp.id = :corpId")
    Optional<Wallet> findByCorpIdWithLock(@Param("corpId") UUID corpId);
}
