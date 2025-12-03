package hu.finex.main.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.AccountListItemResponse;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.dto.UpdateCardNumberRequest;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.AccountMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        String generatedAccountNumber = generateAccountNumber();

        Account account = accountMapper.toEntity(request, user, generatedAccountNumber);
        account.setStatus(AccountStatus.ACTIVE);

        account = accountRepository.save(account);

        return accountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Számla nem található."));

        return accountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountListItemResponse> listByUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        List<Account> accounts = accountRepository.findByUser_Id(userId);
        
        return accounts.stream().map(accountMapper::toListItem).toList();
    }

    @Transactional
    public AccountResponse updateCardNumber(Long id, UpdateCardNumberRequest request) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Számla nem található."));

        account.setCardNumber(request.getCardNumber());

        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse updateStatus(Long id, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Számla nem található."));

        account.setStatus(request.getStatus());

        return accountMapper.toResponse(account);
    }

    @Transactional
    public void delete(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Számla nem található."));

        account.setStatus(AccountStatus.CLOSED);
    }
    
    private String generateAccountNumber() {
        // Egyszerű placeholder: "ACC" + időbélyeg
        return "ACC" + System.currentTimeMillis();
    }
}
