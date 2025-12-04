package hu.finex.main.service;

import java.math.BigDecimal;
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
import hu.finex.main.model.enums.AccountType;
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
    
    private String generateHungarianIban() {
        // 24 jegyű random bankszámlaszám
        StringBuilder base = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            base.append((int) (Math.random() * 10));
        }

        String countryCode = "HU";
        String checksumBase = base.toString() + convertLettersToDigits(countryCode + "00");

        int mod = mod97(checksumBase);
        int checksum = 98 - mod;

        String formattedChecksum = String.format("%02d", checksum);

        return countryCode + formattedChecksum + base;
    }
    
    @Transactional
    public Account createDefaultAccount(User user) {
        Account account = Account.builder()
                .user(user)
                .accountNumber(generateHungarianIban())
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .cardNumber(generateCardNumber())
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.save(account);
    }

    // Betűk átalakítása számokká (A=10, B=11...)
    private String convertLettersToDigits(String input) {
        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isLetter(ch)) {
                result.append((ch - 'A') + 10);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    // Nagy szám mod 97
    private int mod97(String input) {
        String remainder = "0";

        for (int i = 0; i < input.length(); i += 7) {
            int end = Math.min(i + 7, input.length());
            String chunk = remainder + input.substring(i, end);
            remainder = String.valueOf(Long.parseLong(chunk) % 97);
        }

        return Integer.parseInt(remainder);
    }
    
    private int generateLuhnCheckDigit(String numberWithoutCheckDigit) {
        int sum = 0;
        boolean alternate = true;

        for (int i = numberWithoutCheckDigit.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(numberWithoutCheckDigit.charAt(i));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }

            sum += n;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    
    private String generateCardNumber() {
        String bin = "489512"; // fiktív BIN
        StringBuilder number = new StringBuilder(bin);

        // 9 véletlen számjegy
        for (int i = 0; i < 9; i++) {
            number.append((int) (Math.random() * 10));
        }
        // Luhn-ellenőrző szám generálása
        int checkDigit = generateLuhnCheckDigit(number.toString());
        number.append(checkDigit);

        return number.toString();
    }

    
    private String generateAccountNumber() {
        // Egyszerű placeholder: "ACC" + időbélyeg
        return "ACC" + System.currentTimeMillis();
    }
}
