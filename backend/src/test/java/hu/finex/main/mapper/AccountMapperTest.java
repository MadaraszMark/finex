package hu.finex.main.mapper;

import hu.finex.main.dto.AccountListItemResponse;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");

        User user = User.builder()
                .id(5L)
                .build();

        Account account = Account.builder()
                .id(10L)
                .user(user)
                .name("Fő számla")
                .accountNumber("ACC123")
                .balance(new BigDecimal("1500.00"))
                .currency("HUF")
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .createdAt(createdAt)
                .build();

        AccountResponse response = mapper.toResponse(account);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(5L, response.getUserId());
        assertEquals("Fő számla", response.getName());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals(new BigDecimal("1500.00"), response.getBalance());
        assertEquals("HUF", response.getCurrency());
        assertEquals(AccountType.SAVINGS, response.getAccountType());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToListItem() {
        Account account = Account.builder()
                .id(20L)
                .name("Euró számla")
                .accountNumber("ACC456")
                .balance(new BigDecimal("300.00"))
                .currency("EUR")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.BLOCKED)
                .build();

        AccountListItemResponse response = mapper.toListItem(account);

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("Euró számla", response.getName());
        assertEquals("ACC456", response.getAccountNumber());
        assertEquals(new BigDecimal("300.00"), response.getBalance());
        assertEquals("EUR", response.getCurrency());
        assertEquals(AccountType.CURRENT, response.getAccountType());
        assertEquals(AccountStatus.BLOCKED, response.getStatus());
    }

    @Test
    void testToEntity() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .name("Dollár számla")
                .currency("USD")
                .build();

        User user = User.builder()
                .id(3L)
                .build();

        String generatedAccountNumber = "ACC999";

        Account account = mapper.toEntity(request, user, generatedAccountNumber);

        assertNotNull(account);
        assertNull(account.getId());
        assertEquals(user, account.getUser());
        assertEquals("Dollár számla", account.getName());
        assertEquals("ACC999", account.getAccountNumber());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("USD", account.getCurrency());
        assertEquals(AccountType.CURRENT, account.getAccountType());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void testUpdateStatus() {
        Account account = Account.builder()
                .status(AccountStatus.FROZEN)
                .build();

        UpdateAccountStatusRequest request = UpdateAccountStatusRequest.builder()
                .status(AccountStatus.ACTIVE)
                .build();

        mapper.updateStatus(account, request);

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }
}
