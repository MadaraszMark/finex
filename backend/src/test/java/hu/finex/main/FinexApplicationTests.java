package hu.finex.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import hu.finex.main.repository.PostgresRepositoryTestBase;

// A teljes alkalmazás elindul egy tesztkonténeres PostgreSQL-lel: a Flyway lefuttatja a migrációkat,
// a Hibernate pedig ellenőrzi, hogy minden entitás megfelel a sémának (ddl-auto=validate).
// A kontextus a teszt után bezárul: a JPA auditing (AuditingEntityListener) egy statikus aspektuson keresztül
// kapja a beállításait, és nyitva hagyva átszivárogna a később induló @DataJpaTest tesztekbe (ott ki van kapcsolva).
@SpringBootTest
@DirtiesContext
class FinexApplicationTests extends PostgresRepositoryTestBase {

	@Test
	void contextLoads() {
	}

}
