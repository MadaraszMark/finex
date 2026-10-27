package hu.finex.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import hu.finex.main.repository.PostgresRepositoryTestBase;

// A kontextus a teszt után bezárul, különben a JPA auditing átszivárogna a @DataJpaTest tesztekbe
@SpringBootTest
@DirtiesContext
class FinexApplicationTests extends PostgresRepositoryTestBase {

	@Test
	void contextLoads() {
	}

}
