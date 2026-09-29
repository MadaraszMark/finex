package hu.finex.main.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.AccountListItemResponse;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .userId(account.getUser().getId())
                .name(account.getName())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public AccountListItemResponse toListItem(Account account) {
        return AccountListItemResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .build();
    }

    public Account toEntity(CreateAccountRequest request, User user, String generatedAccountNumber) {
        return Account.builder()
                .user(user)
                .name(request.getName())
                .accountNumber(generatedAccountNumber)
                .balance(java.math.BigDecimal.ZERO)
                .currency(request.getCurrency())
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    public void updateStatus(Account account, UpdateAccountStatusRequest request) {
        account.setStatus(request.getStatus());
    }
}
