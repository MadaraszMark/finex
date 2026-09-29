package hu.finex.main.repository;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

// Valódi PostgreSQL (ugyanaz a főverzió, mint a docker-compose-ban) Docker-konténerben.
// A séma a Flyway-migrációkból épül fel (demóadatok nélkül), a Hibernate csak ellenőrzi,
// így a tesztek a valódi megszorításokkal, triggerekkel és függvényekkel futnak.
// A konténer egyszer indul el a teljes tesztfutásra (singleton), mert a Spring a tesztosztályok között
// újrahasznosítja a kontextust: osztályonként újrainduló konténernél a kontextus a régi portra mutatna.

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class PostgresRepositoryTestBase {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("finex_test")
            .withUsername("finex")
            .withPassword("finex");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.show-sql", () -> "false");
    }
}
