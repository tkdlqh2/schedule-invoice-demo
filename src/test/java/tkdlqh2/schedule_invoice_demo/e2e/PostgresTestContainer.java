package tkdlqh2.schedule_invoice_demo.e2e;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton PostgreSQL TestContainer
 * 모든 테스트 클래스가 동일한 컨테이너를 공유하여 성능 향상 및 연결 안정성 보장
 */
public class PostgresTestContainer {

    private static final String IMAGE_VERSION = "postgres:16-alpine";
    private static PostgreSQLContainer<?> container;

    private PostgresTestContainer() {
        // Singleton pattern - private constructor
    }

    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>(IMAGE_VERSION)
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);  // 컨테이너 재사용 활성화
        }
        return container;
    }

    public static void start() {
        getInstance().start();
    }

    public static void stop() {
        if (container != null) {
            container.stop();
        }
    }
}