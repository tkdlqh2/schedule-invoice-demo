package tkdlqh2.schedule_invoice_demo.corp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CorpRepository extends JpaRepository<Corp, UUID> {

    /**
     * 이름으로 기관 조회
     */
    Optional<Corp> findByName(String name);

    /**
     * 사업자 번호로 기관 조회
     */
    Optional<Corp> findByBusinessNumber(String businessNumber);
}
