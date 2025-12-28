package tkdlqh2.schedule_invoice_demo.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import tkdlqh2.schedule_invoice_demo.e2e.PostgresTestContainer;

/**
 * TestContainers 기반 통합 테스트 베이스 클래스
 * - Singleton PostgreSQL 컨테이너 사용 (모든 테스트 클래스가 공유)
 * - Spring Boot 애플리케이션 컨텍스트 로드 (웹 환경 없음)
 * - E2E 테스트와 달리 실제 웹 서버를 띄우지 않음
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    private static final PostgreSQLContainer<?> postgres = PostgresTestContainer.getInstance();

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}