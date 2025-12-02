package hu.finex.main.mapper;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.AccountListItemResponse;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.dto.UpdateCardNumberRequest;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .userId(account.getUser().getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .accountType(account.getAccountType())
                .cardNumber(account.getCardNumber())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public AccountListItemResponse toListItem(Account account) {
        return AccountListItemResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .build();
    }

    public Account toEntity(CreateAccountRequest request, User user, String generatedAccountNumber) {
        return Account.builder()
                .user(user)
                .accountNumber(generatedAccountNumber)
                .balance(java.math.BigDecimal.ZERO)
                .currency(request.getCurrency())
                .accountType(request.getAccountType())
                .cardNumber(null)
                .status(null)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    public void updateCardNumber(Account account, UpdateCardNumberRequest request) {
        account.setCardNumber(request.getCardNumber());
    }

    public void updateStatus(Account account, UpdateAccountStatusRequest request) {
        account.setStatus(request.getStatus());
    }
}
