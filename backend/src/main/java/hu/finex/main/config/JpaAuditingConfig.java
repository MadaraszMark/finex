package hu.finex.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// JPA Auditing külön konfigurációban van, és teszt profil alatt ki van kapcsolva.

@Configuration
@Profile("!test")
@EnableJpaAuditing
public class JpaAuditingConfig {}
