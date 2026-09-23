package hu.finex.main.security;

import hu.finex.main.exception.NotFoundException;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.User;
import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    public Long requireId() {
        return requireEntity().getId();
    }

    public User requireEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new BusinessException("Nincs bejelentkezve.");
        }

        String email = auth.getPrincipal().toString();

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Felhasználó nem található: " + email));
    }

    public void ensureSameUser(Long pathUserId) {
        Long currentId = requireId();
        if (!currentId.equals(pathUserId)) {
            throw new BusinessException("Más felhasználó erőforrása.");
        }
    }
}
