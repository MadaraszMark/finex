package hu.finex.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FinexApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinexApplication.class, args);
	}

}
