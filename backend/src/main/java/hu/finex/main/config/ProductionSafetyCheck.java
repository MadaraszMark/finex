package hu.finex.main.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;

// Prod profil alatt nem indul el JWT-kulcs nélkül, vagy ha demófiókok vannak az adatbázisban

@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProductionSafetyCheck implements ApplicationRunner {

    private static final List<String> DEMO_EMAILS = List.of("admin@finex.hu", "anna@finex.hu", "bence@finex.hu");

    private final UserRepository userRepository;

    @Value("${auth.jwt.secret:}")
    private String jwtSecret;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("Éles üzemben kötelező a FINEX_JWT_SECRET környezeti változó.");
        }
        for (String email : DEMO_EMAILS) {
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalStateException("Éles üzemben nem lehetnek demófiókok az adatbázisban (" + email + ").");
            }
        }
    }
}
