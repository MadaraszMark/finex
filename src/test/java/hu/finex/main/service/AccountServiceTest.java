package hu.finex.main.service;

import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.AccountMapper;
import hu.finex.main.model.*;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BalanceHistoryRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BalanceHistoryRepository balanceHistoryRepository;

    @InjectMocks private AccountService service;

    @Test
    void create_shouldCreateAccountAndSetStatusActive() {
        CreateAccountRequest req = CreateAccountRequest.builder()
                .userId(1L)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .build();

        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Account mapped = Account.builder()
                .user(user)
                .accountNumber("WHATEVER")
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .build();
        when(accountMapper.toEntity(eq(req), eq(user), anyString())).thenReturn(mapped);

        Account saved = Account.builder()
                .id(10L)
                .user(user)
                .accountNumber(mapped.getAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse expectedResp = AccountResponse.builder()
                .id(10L)
                .userId(1L)
                .accountNumber(saved.getAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .build();
        when(accountMapper.toResponse(saved)).thenReturn(expectedResp);

        AccountResponse resp = service.create(req);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals(1L, resp.getUserId());
        assertEquals(AccountStatus.ACTIVE, resp.getStatus());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals(AccountStatus.ACTIVE, accountCaptor.getValue().getStatus());

        verify(userRepository).findById(1L);
        verify(accountMapper).toEntity(eq(req), eq(user), anyString());
        verify(accountMapper).toResponse(saved);
        verifyNoMoreInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void create_shouldThrowNotFound_whenUserMissing() {
        CreateAccountRequest req = CreateAccountRequest.builder()
                .userId(99L)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(userRepository).findById(99L);
        verifyNoInteractions(accountRepository, accountMapper, transactionRepository, balanceHistoryRepository);
    }

    @Test
    void getById_shouldReturnResponse() {
        Account acc = Account.builder().id(5L).build();
        when(accountRepository.findById(5L)).thenReturn(Optional.of(acc));

        AccountResponse expected = AccountResponse.builder().id(5L).build();
        when(accountMapper.toResponse(acc)).thenReturn(expected);

        AccountResponse resp = service.getById(5L);

        assertEquals(5L, resp.getId());
        verify(accountRepository).findById(5L);
        verify(accountMapper).toResponse(acc);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(5L));
        verify(accountRepository).findById(5L);
        verifyNoInteractions(accountMapper);
    }

    @Test
    void getMyAccount_shouldQueryActiveCurrentAndReturn() {
        Account acc = Account.builder().id(1L).build();

        when(accountRepository.findFirstByUser_IdAndStatusAndAccountType(
                7L, AccountStatus.ACTIVE, AccountType.CURRENT
        )).thenReturn(Optional.of(acc));

        AccountResponse expected = AccountResponse.builder().id(1L).build();
        when(accountMapper.toResponse(acc)).thenReturn(expected);

        AccountResponse resp = service.getMyAccount(7L);

        assertEquals(1L, resp.getId());
        verify(accountRepository).findFirstByUser_IdAndStatusAndAccountType(
                7L, AccountStatus.ACTIVE, AccountType.CURRENT
        );
        verify(accountMapper).toResponse(acc);
    }

    @Test
    void getMyAccount_shouldThrowNotFound_whenNoActiveCurrent() {
        when(accountRepository.findFirstByUser_IdAndStatusAndAccountType(
                7L, AccountStatus.ACTIVE, AccountType.CURRENT
        )).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getMyAccount(7L));
    }

    @Test
    void listByUser_shouldThrowNotFound_whenUserMissing() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.listByUser(3L));

        verify(userRepository).findById(3L);
        verifyNoInteractions(accountRepository, accountMapper);
    }

    @Test
    void listByUser_shouldMapAccountsToListItems() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(User.builder().id(3L).build()));

        Account a1 = Account.builder().id(1L).accountNumber("A1").build();
        Account a2 = Account.builder().id(2L).accountNumber("A2").build();
        when(accountRepository.findByUser_Id(3L)).thenReturn(List.of(a1, a2));

        AccountListItemResponse r1 = AccountListItemResponse.builder().id(1L).accountNumber("A1").build();
        AccountListItemResponse r2 = AccountListItemResponse.builder().id(2L).accountNumber("A2").build();
        when(accountMapper.toListItem(a1)).thenReturn(r1);
        when(accountMapper.toListItem(a2)).thenReturn(r2);

        List<AccountListItemResponse> out = service.listByUser(3L);

        assertEquals(2, out.size());
        assertEquals(1L, out.get(0).getId());
        assertEquals(2L, out.get(1).getId());

        verify(userRepository).findById(3L);
        verify(accountRepository).findByUser_Id(3L);
        verify(accountMapper).toListItem(a1);
        verify(accountMapper).toListItem(a2);
    }

    @Test
    void updateCardNumber_shouldUpdateFieldAndReturnResponse() {
        Account acc = Account.builder().id(1L).cardNumber(null).build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));

        UpdateCardNumberRequest req = UpdateCardNumberRequest.builder().cardNumber("4895121234567890").build();

        AccountResponse expected = AccountResponse.builder().id(1L).cardNumber("4895121234567890").build();
        when(accountMapper.toResponse(acc)).thenReturn(expected);

        AccountResponse resp = service.updateCardNumber(1L, req);

        assertEquals("4895121234567890", acc.getCardNumber());
        assertEquals("4895121234567890", resp.getCardNumber());
        verify(accountRepository).findById(1L);
        verify(accountMapper).toResponse(acc);
    }

    @Test
    void updateCardNumber_shouldThrowNotFound_whenMissing() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                service.updateCardNumber(1L, UpdateCardNumberRequest.builder().cardNumber("x").build()));
    }

    @Test
    void updateStatus_shouldUpdateFieldAndReturnResponse() {
        Account acc = Account.builder().id(2L).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findById(2L)).thenReturn(Optional.of(acc));

        UpdateAccountStatusRequest req = UpdateAccountStatusRequest.builder().status(AccountStatus.FROZEN).build();

        AccountResponse expected = AccountResponse.builder().id(2L).status(AccountStatus.FROZEN).build();
        when(accountMapper.toResponse(acc)).thenReturn(expected);

        AccountResponse resp = service.updateStatus(2L, req);

        assertEquals(AccountStatus.FROZEN, acc.getStatus());
        assertEquals(AccountStatus.FROZEN, resp.getStatus());
    }

    @Test
    void delete_shouldSetStatusClosed() {
        Account acc = Account.builder().id(3L).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findById(3L)).thenReturn(Optional.of(acc));

        service.delete(3L);

        assertEquals(AccountStatus.CLOSED, acc.getStatus());
        verify(accountRepository).findById(3L);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void deposit_shouldAddBalanceAndPersistTxAndHistory_andReturnResponse() {
        Long accountId = 10L;
        Long userId = 7L;

        User owner = User.builder().id(userId).build();
        Account acc = Account.builder()
                .id(accountId)
                .user(owner)
                .balance(new BigDecimal("1000.00"))
                .currency("HUF")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(acc));

        BigDecimal amount = new BigDecimal("250.00");
        String message = "Topup";

        AccountResponse expected = AccountResponse.builder()
                .id(accountId)
                .balance(new BigDecimal("1250.00"))
                .currency("HUF")
                .build();
        when(accountMapper.toResponse(acc)).thenReturn(expected);

        AccountResponse resp = service.deposit(accountId, amount, message, userId);

        assertEquals(new BigDecimal("1250.00"), acc.getBalance());
        assertEquals(new BigDecimal("1250.00"), resp.getBalance());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();
        assertEquals(acc, savedTx.getAccount());
        assertEquals(TransactionType.INCOME, savedTx.getType());
        assertEquals(amount, savedTx.getAmount());
        assertEquals("HUF", savedTx.getCurrency());
        assertEquals(message, savedTx.getMessage());

        ArgumentCaptor<BalanceHistory> historyCaptor = ArgumentCaptor.forClass(BalanceHistory.class);
        verify(balanceHistoryRepository).save(historyCaptor.capture());
        BalanceHistory savedHistory = historyCaptor.getValue();
        assertEquals(acc, savedHistory.getAccount());
        assertEquals(new BigDecimal("1250.00"), savedHistory.getBalance());

        verify(accountRepository).findById(accountId);
        verify(accountMapper).toResponse(acc);
    }

    @Test
    void deposit_shouldThrowNotFound_whenAccountMissing() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.deposit(1L, BigDecimal.ONE, "x", 1L));
        verify(accountRepository).findById(1L);
        verifyNoInteractions(transactionRepository, balanceHistoryRepository, accountMapper);
    }

    @Test
    void deposit_shouldThrowBusinessException_whenNotOwner() {
        Long accountId = 10L;

        User owner = User.builder().id(999L).build();
        Account acc = Account.builder()
                .id(accountId)
                .user(owner)
                .balance(new BigDecimal("1000.00"))
                .currency("HUF")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(acc));

        assertThrows(BusinessException.class, () ->
                service.deposit(accountId, new BigDecimal("100.00"), "x", 7L));

        assertEquals(new BigDecimal("1000.00"), acc.getBalance());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository, accountMapper);
    }
}
