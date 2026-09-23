package hu.finex.main.service;

import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.User;
import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailChanged && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Ezzel az email címmel már létezik felhasználó.");
        }

        userMapper.updateEntity(user, request);

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Felhasználó nem található.");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserResponse getOwnProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));
        return userMapper.toResponse(user);
    }
}

