package hu.finex.main.service;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.SavingsAccountMapper;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.repository.SavingsAccountRepository;
import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingsAccountService {

    private final SavingsAccountRepository savingsAccountRepository;
    private final UserRepository userRepository;
    private final SavingsAccountMapper mapper;

    @Transactional
    public SavingsAccountResponse create(CreateSavingsAccountRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        if (savingsAccountRepository.existsByUser_IdAndName(request.getUserId(), request.getName())) {
            throw new BusinessException("Már létezik ilyen nevű megtakarítás ennél a felhasználónál.");
        }

        SavingsAccount account = mapper.toEntity(request, user);
        account.setStatus(SavingsStatus.ACTIVE);

        account = savingsAccountRepository.save(account);
        return mapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public SavingsAccountResponse getById(Long id) {
        SavingsAccount entity = savingsAccountRepository.findById(id).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));

        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<SavingsAccountResponse> listByUser(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return savingsAccountRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SavingsAccountResponse> listByUserAndStatus(Long userId, SavingsStatus status, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return savingsAccountRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, status, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SavingsAccountResponse> listAboveBalance(Long userId, java.math.BigDecimal minBalance, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return savingsAccountRepository.findByUser_IdAndBalanceGreaterThanEqual(userId, minBalance, pageable).map(mapper::toResponse);
    }

    @Transactional
    public SavingsAccountResponse update(Long id, UpdateSavingsAccountRequest request) {

        SavingsAccount entity = savingsAccountRepository.findById(id).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));

        if (!entity.getName().equalsIgnoreCase(request.getName())&& savingsAccountRepository.existsByUser_IdAndName(entity.getUser().getId(), request.getName())) {
            throw new BusinessException("Ezzel a névvel már létezik megtakarítás.");
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        SavingsAccount entity = savingsAccountRepository.findById(id).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));

        entity.setStatus(SavingsStatus.CLOSED);
    }
}
