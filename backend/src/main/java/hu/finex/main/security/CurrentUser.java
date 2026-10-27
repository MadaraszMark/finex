package hu.finex.main.security;

import hu.finex.main.exception.NotFoundException;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.User;
import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// A "saját" adatokat mindig a tokenből vett azonosítóval kérdezzük le, nem a kliens által küldöttel

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    // A JwtAuthenticationFilter a felhasználó azonosítóját teszi principalként a SecurityContext-be
    public Long requireId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Long)) {
            throw new BusinessException("Nincs bejelentkezve.");
        }

        return (Long) auth.getPrincipal();
    }

    public User requireEntity() {
        Long userId = requireId();

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Felhasználó nem található: " + userId));
    }

    public void ensureSameUser(Long pathUserId) {
        Long currentId = requireId();
        if (!currentId.equals(pathUserId)) {
            throw new BusinessException("Más felhasználó erőforrása.");
        }
    }
}
