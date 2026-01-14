package hu.finex.main.service;

import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.*;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BalanceHistoryRepository balanceHistoryRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private BalanceHistoryMapper balanceHistoryMapper;
    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private TransactionService service;

    @Test
    void create_shouldThrowNotFound_whenAccountMissing() {
        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .currency("HUF")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(accountRepository).findById(1L);
        verifyNoInteractions(balanceHistoryRepository, transactionRepository, transactionMapper, balanceHistoryMapper, transactionCategoryRepository, categoryRepository);
    }

    @Test
    void create_shouldThrowBusinessException_whenCurrencyMismatch() {
        Account account = Account.builder()
                .id(1L)
                .currency("HUF")
                .balance(new BigDecimal("100.00"))
                .build();

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .currency("EUR")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(BusinessException.class, () -> service.create(req));

        verify(accountRepository).findById(1L);
        verifyNoInteractions(balanceHistoryRepository, transactionRepository, transactionMapper, balanceHistoryMapper, transactionCategoryRepository, categoryRepository);
    }

    @Test
    void create_shouldIncreaseBalance_andSaveHistoryAndTx_andReturnResponse_whenIncome() {
        Account account = Account.builder()
                .id(1L)
                .currency("HUF")
                .balance(new BigDecimal("100.00"))
                .build();

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .currency("HUF")
                .message("income")
                .categoryIds(null)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BalanceHistory history = BalanceHistory.builder().account(account).balance(new BigDecimal("110.00")).build();
        when(balanceHistoryMapper.toEntity(account, new BigDecimal("110.00"))).thenReturn(history);
        when(balanceHistoryRepository.save(history)).thenReturn(history);

        Transaction txEntity = Transaction.builder().account(account).build();
        when(transactionMapper.toEntity(req, account)).thenReturn(txEntity);

        Transaction savedTx = Transaction.builder()
                .id(10L)
                .account(account)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .currency("HUF")
                .message("income")
                .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
                .build();
        when(transactionRepository.save(txEntity)).thenReturn(savedTx);

        when(transactionCategoryRepository.findByTransaction_Id(10L)).thenReturn(List.of());

        TransactionResponse mappedResp = TransactionResponse.builder()
                .id(10L)
                .accountId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .currency("HUF")
                .message("income")
                .createdAt(savedTx.getCreatedAt())
                .build();
        when(transactionMapper.toResponse(savedTx)).thenReturn(mappedResp);

        TransactionResponse resp = service.create(req);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals(new BigDecimal("110.00"), account.getBalance());
        assertNotNull(resp.getCategories());
        assertEquals(0, resp.getCategories().size());

        verify(balanceHistoryMapper).toEntity(account, new BigDecimal("110.00"));
        verify(balanceHistoryRepository).save(history);
        verify(transactionRepository).save(txEntity);
        verify(transactionMapper).toResponse(savedTx);
        verify(transactionCategoryRepository).findByTransaction_Id(10L);
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void create_shouldThrowBusinessException_whenInsufficientFundsForOutcome() {
        Account account = Account.builder()
                .id(1L)
                .currency("HUF")
                .balance(new BigDecimal("50.00"))
                .build();

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountId(1L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("100.00"))
                .currency("HUF")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(BusinessException.class, () -> service.create(req));

        assertEquals(new BigDecimal("50.00"), account.getBalance());
        verifyNoInteractions(balanceHistoryRepository, transactionRepository, transactionMapper, balanceHistoryMapper, transactionCategoryRepository, categoryRepository);
    }

    @Test
    void create_shouldDecreaseBalance_andSaveHistoryAndTx_andSaveCategories_whenOutcomeWithCategories() {
        Account account = Account.builder()
                .id(1L)
                .currency("HUF")
                .balance(new BigDecimal("100.00"))
                .accountNumber("ACC-1")
                .build();

        List<Long> categoryIds = List.of(7L, 8L);

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountId(1L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("30.00"))
                .currency("HUF")
                .message("buy")
                .categoryIds(categoryIds)
                .fromAccount("ACC-1")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BalanceHistory history = BalanceHistory.builder().account(account).balance(new BigDecimal("70.00")).build();
        when(balanceHistoryMapper.toEntity(account, new BigDecimal("70.00"))).thenReturn(history);
        when(balanceHistoryRepository.save(history)).thenReturn(history);

        Transaction txEntity = Transaction.builder().account(account).build();
        when(transactionMapper.toEntity(req, account)).thenReturn(txEntity);

        Transaction savedTx = Transaction.builder()
                .id(10L)
                .account(account)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("30.00"))
                .currency("HUF")
                .message("buy")
                .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
                .build();
        when(transactionRepository.save(txEntity)).thenReturn(savedTx);

        when(categoryRepository.existsById(7L)).thenReturn(true);
        when(categoryRepository.existsById(8L)).thenReturn(true);

        Category c7 = Category.builder().id(7L).name("Food").icon("🍔").build();
        Category c8 = Category.builder().id(8L).name("Transport").icon("🚗").build();
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(c7));
        when(categoryRepository.findById(8L)).thenReturn(Optional.of(c8));

        when(transactionCategoryRepository.save(any(TransactionCategory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionCategory link1 = TransactionCategory.builder().transaction(savedTx).category(c7).build();
        TransactionCategory link2 = TransactionCategory.builder().transaction(savedTx).category(c8).build();
        when(transactionCategoryRepository.findByTransaction_Id(10L)).thenReturn(List.of(link1, link2));

        TransactionResponse mappedResp = TransactionResponse.builder()
                .id(10L)
                .accountId(1L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("30.00"))
                .currency("HUF")
                .message("buy")
                .createdAt(savedTx.getCreatedAt())
                .build();
        when(transactionMapper.toResponse(savedTx)).thenReturn(mappedResp);

        TransactionResponse resp = service.create(req);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals(new BigDecimal("70.00"), account.getBalance());
        assertNotNull(resp.getCategories());
        assertEquals(2, resp.getCategories().size());
        assertEquals(7L, resp.getCategories().get(0).getId());
        assertEquals(8L, resp.getCategories().get(1).getId());

        verify(categoryRepository).existsById(7L);
        verify(categoryRepository).existsById(8L);
        verify(categoryRepository).findById(7L);
        verify(categoryRepository).findById(8L);

        ArgumentCaptor<TransactionCategory> tcCaptor = ArgumentCaptor.forClass(TransactionCategory.class);
        verify(transactionCategoryRepository, times(2)).save(tcCaptor.capture());
        List<TransactionCategory> savedLinks = tcCaptor.getAllValues();
        assertEquals(savedTx, savedLinks.get(0).getTransaction());
        assertEquals(savedTx, savedLinks.get(1).getTransaction());

        verify(transactionCategoryRepository).findByTransaction_Id(10L);
    }

    @Test
    void create_shouldThrowNotFound_whenCategoryMissing_inSaveCategories() {
        Account account = Account.builder()
                .id(1L)
                .currency("HUF")
                .balance(new BigDecimal("100.00"))
                .build();

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountId(1L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("10.00"))
                .currency("HUF")
                .categoryIds(List.of(99L))
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(categoryRepository).existsById(99L);
        verify(categoryRepository, never()).findById(anyLong());
        verify(transactionCategoryRepository, never()).save(any());
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(transactionRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(5L));
    }

    @Test
    void getById_shouldReturnResponseWithCategories() {
        Account account = Account.builder().id(1L).build();
        Transaction tx = Transaction.builder()
                .id(10L)
                .account(account)
                .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
                .build();

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(tx));

        Category c1 = Category.builder().id(1L).name("Food").icon("🍔").build();
        Category c2 = Category.builder().id(2L).name("Transport").icon("🚗").build();
        TransactionCategory link1 = TransactionCategory.builder().transaction(tx).category(c1).build();
        TransactionCategory link2 = TransactionCategory.builder().transaction(tx).category(c2).build();
        when(transactionCategoryRepository.findByTransaction_Id(10L)).thenReturn(List.of(link1, link2));

        TransactionResponse mapped = TransactionResponse.builder().id(10L).accountId(1L).build();
        when(transactionMapper.toResponse(tx)).thenReturn(mapped);

        TransactionResponse resp = service.getById(10L);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertNotNull(resp.getCategories());
        assertEquals(2, resp.getCategories().size());
        assertEquals(1L, resp.getCategories().get(0).getId());
        assertEquals(2L, resp.getCategories().get(1).getId());

        verify(transactionRepository).findById(10L);
        verify(transactionCategoryRepository).findByTransaction_Id(10L);
        verify(transactionMapper).toResponse(tx);
    }

    @Test
    void listByAccount_shouldThrowNotFound_whenAccountMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.listByAccount(5L, pageable));

        verify(accountRepository).findById(5L);
        verifyNoInteractions(transactionRepository, transactionMapper);
    }

    @Test
    void listByAccount_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        when(accountRepository.findById(5L)).thenReturn(Optional.of(Account.builder().id(5L).build()));

        Transaction t1 = Transaction.builder().id(1L).build();
        Transaction t2 = Transaction.builder().id(2L).build();
        Page<Transaction> page = new PageImpl<>(List.of(t1, t2), pageable, 2);

        when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(5L, pageable)).thenReturn(page);

        TransactionListItemResponse r1 = TransactionListItemResponse.builder().id(1L).build();
        TransactionListItemResponse r2 = TransactionListItemResponse.builder().id(2L).build();
        when(transactionMapper.toListItem(t1)).thenReturn(r1);
        when(transactionMapper.toListItem(t2)).thenReturn(r2);

        Page<TransactionListItemResponse> resp = service.listByAccount(5L, pageable);

        assertEquals(2, resp.getTotalElements());
        assertSame(r1, resp.getContent().get(0));
        assertSame(r2, resp.getContent().get(1));

        verify(transactionRepository).findByAccount_IdOrderByCreatedAtDesc(5L, pageable);
        verify(transactionMapper).toListItem(t1);
        verify(transactionMapper).toListItem(t2);
    }

    @Test
    void transfer_shouldThrowNotFound_whenFromMissing() {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .currency("HUF")
                .amount(new BigDecimal("10.00"))
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.transfer(req));

        verify(accountRepository).findById(1L);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void transfer_shouldThrowNotFound_whenToMissing() {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .currency("HUF")
                .amount(new BigDecimal("10.00"))
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(Account.builder().id(1L).build()));
        when(accountRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.transfer(req));

        verify(accountRepository).findById(1L);
        verify(accountRepository).findById(2L);
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }


    @Test
    void transfer_shouldThrowBusinessException_whenCurrencyMismatch() {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .currency("HUF")
                .amount(new BigDecimal("10.00"))
                .build();

        Account from = Account.builder().id(1L).currency("EUR").build();
        Account to = Account.builder().id(2L).currency("HUF").build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(BusinessException.class, () -> service.transfer(req));

        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void transfer_shouldThrowBusinessException_whenInsufficientFunds() {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .currency("HUF")
                .amount(new BigDecimal("10.00"))
                .build();

        Account from = Account.builder().id(1L).currency("HUF").balance(new BigDecimal("5.00")).build();
        Account to = Account.builder().id(2L).currency("HUF").balance(new BigDecimal("0.00")).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(BusinessException.class, () -> service.transfer(req));

        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    
    
}
