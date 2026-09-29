package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.CategoryMapper;
import hu.finex.main.mapper.TransactionCategoryMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.*;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String ANNA_IBAN = "HU15117730161111101800000001";
    private static final String ANNA_EUR_IBAN = "HU85117730161111101800000002";
    private static final String BENCE_IBAN = "HU28104000950000521700000003";
    private static final String OTHER_IBAN = "HU50120100070000123400000004";
    private static final String LANDLORD_IBAN = "HU66109180010000048900240017";

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private TransactionCategoryMapper transactionCategoryMapper;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;
    @Mock private LedgerService ledgerService;
    @Mock private NotificationService notificationService;
    @Mock private CurrentUser currentUser;
    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private TransactionService service;

    @Test
    void getById_shouldReturnOwnTransactionWithCategories() {
        when(currentUser.requireId()).thenReturn(7L);

        Transaction tx = Transaction.builder().id(99L).build();
        when(transactionRepository.findByIdAndAccount_User_Id(99L, 7L)).thenReturn(Optional.of(tx));

        Category food = Category.builder().id(1L).name("Élelmiszer").build();
        when(transactionCategoryRepository.findByTransaction_IdIn(List.of(99L))).thenReturn(List.of(TransactionCategory.builder().transaction(tx).category(food).build()));

        CategoryResponse foodResp = CategoryResponse.builder().id(1L).name("Élelmiszer").build();
        when(categoryMapper.toResponse(food)).thenReturn(foodResp);

        TransactionResponse expected = TransactionResponse.builder().id(99L).categories(List.of(foodResp)).build();
        when(transactionMapper.toResponse(tx, List.of(foodResp))).thenReturn(expected);

        TransactionResponse resp = service.getById(99L);

        assertEquals(expected, resp);
    }

    @Test
    void getById_shouldThrowNotFound_whenTransactionBelongsToOtherUser() {
        when(currentUser.requireId()).thenReturn(7L);
        when(transactionRepository.findByIdAndAccount_User_Id(99L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(99L));

        verifyNoInteractions(transactionMapper, transactionCategoryRepository);
    }

    @Test
    void search_shouldNormalizeFilters_andLoadCategoriesWithOneQuery() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(true);

        TransactionSearchRequest filter = TransactionSearchRequest.builder()
                .accountId(3L)
                .from(LocalDate.of(2025, 3, 1))
                .to(LocalDate.of(2025, 3, 31))
                .search("  Tesco ")
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        Transaction t1 = Transaction.builder().id(1L).build();
        Transaction t2 = Transaction.builder().id(2L).build();

        // A záró nap is benne van: a felső határ a következő nap kezdete (budapesti idő szerint)
        when(transactionRepository.search(7L, 3L, null, DateUtils.startOfDay(LocalDate.of(2025, 3, 1)), DateUtils.startOfDay(LocalDate.of(2025, 4, 1)),
                null, null, "%tesco%", null, pageable)).thenReturn(new PageImpl<>(List.of(t1, t2), pageable, 2));

        Category food = Category.builder().id(1L).build();
        when(transactionCategoryRepository.findByTransaction_IdIn(List.of(1L, 2L))).thenReturn(List.of(TransactionCategory.builder().transaction(t1).category(food).build()));

        CategoryResponse foodResp = CategoryResponse.builder().id(1L).build();
        when(categoryMapper.toResponse(food)).thenReturn(foodResp);

        when(transactionMapper.toListItem(t1, List.of(foodResp))).thenReturn(TransactionListItemResponse.builder().id(1L).categories(List.of(foodResp)).build());
        when(transactionMapper.toListItem(t2, List.of())).thenReturn(TransactionListItemResponse.builder().id(2L).categories(List.of()).build());

        Page<TransactionListItemResponse> page = service.search(filter, pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().get(0).getCategories().size());
        assertTrue(page.getContent().get(1).getCategories().isEmpty());
        verify(transactionCategoryRepository, times(1)).findByTransaction_IdIn(anyCollection());
    }

    @Test
    void search_shouldThrowNotFound_whenFilteringForOtherUsersAccount() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(false);

        TransactionSearchRequest filter = TransactionSearchRequest.builder().accountId(3L).build();

        assertThrows(NotFoundException.class, () -> service.search(filter, PageRequest.of(0, 20)));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void search_shouldThrowBusinessException_whenFromIsAfterTo() {
        when(currentUser.requireId()).thenReturn(7L);

        TransactionSearchRequest filter = TransactionSearchRequest.builder()
                .from(LocalDate.of(2025, 4, 1))
                .to(LocalDate.of(2025, 3, 1))
                .build();

        assertThrows(BusinessException.class, () -> service.search(filter, PageRequest.of(0, 20)));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_shouldDebitAndCredit_andNotifyRecipient_whenTargetIsFinexAccount() {
        User anna = user(7L, "Anna", "Kovács");
        User bence = user(8L, "Bence", "Nagy");
        when(currentUser.requireEntity()).thenReturn(anna);

        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(BENCE_IBAN)).thenReturn(Optional.of(3L));

        Account from = activeAccount(1L, anna, ANNA_IBAN, "HUF", "100000.00");
        Account to = activeAccount(3L, bence, BENCE_IBAN, "HUF", "0.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(to));
        when(transactionRepository.sumAmountByAccountAndTypeSince(eq(1L), eq(TransactionType.TRANSFER_OUT), any())).thenReturn(BigDecimal.ZERO);

        Transaction outgoing = Transaction.builder().id(100L).createdAt(Instant.parse("2025-03-10T10:00:00Z")).build();
        when(ledgerService.debit(from, TransactionType.TRANSFER_OUT, new BigDecimal("5000.00"), "Nagy Bence", "Pizza", BENCE_IBAN, null)).thenReturn(outgoing);

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber("hu28 1040 0095 0000 5217 0000 0003")
                .partnerName("Nagy Bence")
                .amount(new BigDecimal("5000.00"))
                .message("Pizza")
                .build();

        TransferResponse resp = service.transfer(req);

        assertEquals(100L, resp.getTransactionId());
        assertEquals(1L, resp.getFromAccountId());
        assertEquals(BENCE_IBAN, resp.getToAccountNumber());
        assertEquals("HUF", resp.getCurrency());
        assertTrue(resp.isInternal());
        assertTrue(resp.getCategories().isEmpty());
        assertEquals(Instant.parse("2025-03-10T10:00:00Z"), resp.getCreatedAt());

        verify(ledgerService).credit(to, TransactionType.TRANSFER_IN, new BigDecimal("5000.00"), "Kovács Anna", "Pizza", ANNA_IBAN);
        verify(notificationService).notify(eq(bence), eq(NotificationType.TRANSACTION), eq("Beérkező utalás"), contains("Kovács Anna"));
        verifyNoInteractions(transactionCategoryRepository);
    }

    @Test
    void transfer_shouldOnlyDebit_andSaveCategories_whenTargetIsExternalAccount() {
        User anna = user(7L, "Anna", "Kovács");
        when(currentUser.requireEntity()).thenReturn(anna);

        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(LANDLORD_IBAN)).thenReturn(Optional.empty());

        Account from = activeAccount(1L, anna, ANNA_IBAN, "HUF", "500000.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(transactionRepository.sumAmountByAccountAndTypeSince(eq(1L), eq(TransactionType.TRANSFER_OUT), any())).thenReturn(BigDecimal.ZERO);

        // A duplikált kategória-azonosító csak egyszer számít
        Category housing = Category.builder().id(5L).name("Lakhatás").build();
        when(categoryRepository.findAllById(Set.of(5L))).thenReturn(List.of(housing));

        Transaction outgoing = Transaction.builder().id(101L).build();
        when(ledgerService.debit(from, TransactionType.TRANSFER_OUT, new BigDecimal("220000.00"), "Tóth Gábor", "Albérlet", LANDLORD_IBAN, null)).thenReturn(outgoing);

        TransactionCategory link = TransactionCategory.builder().transaction(outgoing).category(housing).build();
        when(transactionCategoryMapper.toEntity(outgoing, housing)).thenReturn(link);

        CategoryResponse housingResp = CategoryResponse.builder().id(5L).name("Lakhatás").build();
        when(categoryMapper.toResponse(housing)).thenReturn(housingResp);

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber(LANDLORD_IBAN)
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .message("Albérlet")
                .categoryIds(List.of(5L, 5L))
                .build();

        TransferResponse resp = service.transfer(req);

        assertFalse(resp.isInternal());
        assertEquals(List.of(housingResp), resp.getCategories());
        verify(transactionCategoryRepository).save(link);
        verify(ledgerService, never()).credit(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void transfer_shouldNotNotify_whenMovingBetweenOwnAccounts() {
        User anna = user(7L, "Anna", "Kovács");
        when(currentUser.requireEntity()).thenReturn(anna);

        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(OTHER_IBAN)).thenReturn(Optional.of(4L));

        Account from = activeAccount(1L, anna, ANNA_IBAN, "HUF", "10000.00");
        Account to = activeAccount(4L, anna, OTHER_IBAN, "HUF", "0.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(to));
        when(transactionRepository.sumAmountByAccountAndTypeSince(eq(1L), eq(TransactionType.TRANSFER_OUT), any())).thenReturn(BigDecimal.ZERO);
        when(ledgerService.debit(any(), any(), any(), any(), any(), any(), any())).thenReturn(Transaction.builder().id(102L).build());

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber(OTHER_IBAN)
                .partnerName("Saját számla")
                .amount(new BigDecimal("1000.00"))
                .build();

        TransferResponse resp = service.transfer(req);

        assertTrue(resp.isInternal());
        verify(ledgerService).credit(to, TransactionType.TRANSFER_IN, new BigDecimal("1000.00"), "Kovács Anna", null, ANNA_IBAN);
        verifyNoInteractions(notificationService);
    }

    @Test
    void transfer_shouldLockAccountsInIdOrder_toAvoidDeadlock() {
        User anna = user(7L, "Anna", "Kovács");
        User bence = user(8L, "Bence", "Nagy");
        when(currentUser.requireEntity()).thenReturn(anna);

        // A forrás azonosítója nagyobb, mint a célé: ilyenkor a célszámla zárolódik előbb
        when(accountRepository.existsByIdAndUser_Id(5L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(BENCE_IBAN)).thenReturn(Optional.of(3L));

        Account from = activeAccount(5L, anna, ANNA_IBAN, "HUF", "10000.00");
        Account to = activeAccount(3L, bence, BENCE_IBAN, "HUF", "0.00");
        when(accountRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(from));
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(to));
        when(transactionRepository.sumAmountByAccountAndTypeSince(eq(5L), eq(TransactionType.TRANSFER_OUT), any())).thenReturn(BigDecimal.ZERO);
        when(ledgerService.debit(any(), any(), any(), any(), any(), any(), any())).thenReturn(Transaction.builder().id(103L).build());

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(5L)
                .toAccountNumber(BENCE_IBAN)
                .partnerName("Nagy Bence")
                .amount(new BigDecimal("1000.00"))
                .build();

        service.transfer(req);

        InOrder inOrder = inOrder(accountRepository);
        inOrder.verify(accountRepository).findByIdForUpdate(3L);
        inOrder.verify(accountRepository).findByIdForUpdate(5L);
    }

    @Test
    void transfer_shouldThrowBusinessException_whenIbanIsInvalid() {
        when(currentUser.requireEntity()).thenReturn(user(7L, "Anna", "Kovács"));

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber("HU15117730161111101800000002")
                .partnerName("Hibás")
                .amount(BigDecimal.TEN)
                .build();

        assertThrows(BusinessException.class, () -> service.transfer(req));

        verifyNoInteractions(accountRepository, ledgerService);
    }

    @Test
    void transfer_shouldThrowNotFound_whenSourceAccountIsNotOwn() {
        when(currentUser.requireEntity()).thenReturn(user(7L, "Anna", "Kovács"));
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(false);

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(3L)
                .toAccountNumber(ANNA_IBAN)
                .partnerName("Kovács Anna")
                .amount(BigDecimal.TEN)
                .build();

        assertThrows(NotFoundException.class, () -> service.transfer(req));

        verify(accountRepository, never()).findByIdForUpdate(any());
        verifyNoInteractions(ledgerService);
    }

    @Test
    void transfer_shouldThrowBusinessException_whenSourceAndTargetAreTheSame() {
        when(currentUser.requireEntity()).thenReturn(user(7L, "Anna", "Kovács"));
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(ANNA_IBAN)).thenReturn(Optional.of(1L));

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber(ANNA_IBAN)
                .partnerName("Kovács Anna")
                .amount(BigDecimal.TEN)
                .build();

        assertThrows(BusinessException.class, () -> service.transfer(req));

        verify(accountRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void transfer_shouldThrowBusinessException_whenCurrenciesDiffer() {
        User anna = user(7L, "Anna", "Kovács");
        when(currentUser.requireEntity()).thenReturn(anna);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(ANNA_EUR_IBAN)).thenReturn(Optional.of(2L));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(activeAccount(1L, anna, ANNA_IBAN, "HUF", "10000.00")));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(activeAccount(2L, anna, ANNA_EUR_IBAN, "EUR", "0.00")));

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber(ANNA_EUR_IBAN)
                .partnerName("Euró számla")
                .amount(BigDecimal.TEN)
                .build();

        assertThrows(BusinessException.class, () -> service.transfer(req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void transfer_shouldThrowBusinessException_whenRecipientAccountIsNotActive() {
        User anna = user(7L, "Anna", "Kovács");
        when(currentUser.requireEntity()).thenReturn(anna);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(BENCE_IBAN)).thenReturn(Optional.of(3L));

        Account to = activeAccount(3L, user(8L, "Bence", "Nagy"), BENCE_IBAN, "HUF", "0.00");
        to.setStatus(AccountStatus.FROZEN);
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(activeAccount(1L, anna, ANNA_IBAN, "HUF", "10000.00")));
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(to));

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber(BENCE_IBAN)
                .partnerName("Nagy Bence")
                .amount(BigDecimal.TEN)
                .build();

        assertThrows(BusinessException.class, () -> service.transfer(req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void transfer_shouldThrowBusinessException_whenDailyLimitWouldBeExceeded() {
        User anna = user(7L, "Anna", "Kovács");
        when(currentUser.requireEntity()).thenReturn(anna);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(LANDLORD_IBAN)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(activeAccount(1L, anna, ANNA_IBAN, "HUF", "9000000.00")));

        // Ma már 4 990 000 Ft ment ki, a limit 5 000 000 Ft
        when(transactionRepository.sumAmountByAccountAndTypeSince(eq(1L), eq(TransactionType.TRANSFER_OUT), any())).thenReturn(new BigDecimal("4990000.00"));

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber(LANDLORD_IBAN)
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("20000.00"))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(req));

        assertTrue(ex.getMessage().contains("napi utalási limit"));
        verifyNoInteractions(ledgerService);
    }

    @Test
    void transfer_shouldThrowNotFound_whenCategoryIsMissing() {
        User anna = user(7L, "Anna", "Kovács");
        when(currentUser.requireEntity()).thenReturn(anna);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);
        when(accountRepository.findIdByAccountNumber(LANDLORD_IBAN)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(activeAccount(1L, anna, ANNA_IBAN, "HUF", "10000.00")));
        when(transactionRepository.sumAmountByAccountAndTypeSince(eq(1L), eq(TransactionType.TRANSFER_OUT), any())).thenReturn(BigDecimal.ZERO);
        when(categoryRepository.findAllById(Set.of(99L))).thenReturn(List.of());

        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountNumber(LANDLORD_IBAN)
                .partnerName("Tóth Gábor")
                .amount(BigDecimal.TEN)
                .categoryIds(List.of(99L))
                .build();

        assertThrows(NotFoundException.class, () -> service.transfer(req));

        verifyNoInteractions(ledgerService);
    }

    private User user(Long id, String firstName, String lastName) {
        return User.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    private Account activeAccount(Long id, User user, String accountNumber, String currency, String balance) {
        return Account.builder()
                .id(id)
                .user(user)
                .accountNumber(accountNumber)
                .currency(currency)
                .balance(new BigDecimal(balance))
                .status(AccountStatus.ACTIVE)
                .build();
    }
}
