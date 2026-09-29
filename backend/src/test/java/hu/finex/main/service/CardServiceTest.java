package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.CardMapper;
import hu.finex.main.mapper.CategoryMapper;
import hu.finex.main.mapper.TransactionCategoryMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.*;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.CardStatus;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.CardRepository;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private CardMapper cardMapper;
    @Mock private TransactionMapper transactionMapper;
    @Mock private CategoryMapper categoryMapper;
    @Mock private TransactionCategoryMapper transactionCategoryMapper;
    @Mock private LedgerService ledgerService;
    @Mock private CurrentUser currentUser;
    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private CardService service;

    @Test
    void listMine_shouldIncludeTodaysSpending() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        when(cardRepository.findByAccount_User_IdOrderByCreatedAtAsc(7L)).thenReturn(List.of(card));
        when(transactionRepository.sumCardSpendingSince(eq(3L), any())).thenReturn(new BigDecimal("12990.00"));

        CardResponse expected = CardResponse.builder().id(3L).spentToday(new BigDecimal("12990.00")).build();
        when(cardMapper.toResponse(card, new BigDecimal("12990.00"))).thenReturn(expected);

        List<CardResponse> out = service.listMine();

        assertEquals(List.of(expected), out);
    }

    @Test
    void block_shouldBlockActiveCard() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        when(cardRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.of(card));
        when(transactionRepository.sumCardSpendingSince(eq(3L), any())).thenReturn(BigDecimal.ZERO);
        when(cardMapper.toResponse(card, BigDecimal.ZERO)).thenReturn(CardResponse.builder().id(3L).status(CardStatus.BLOCKED).build());

        CardResponse resp = service.block(3L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        assertEquals(CardStatus.BLOCKED, resp.getStatus());
    }

    @Test
    void block_shouldThrowBusinessException_whenCardIsCancelled() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.CANCELLED, AccountStatus.CLOSED);
        when(cardRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.of(card));

        assertThrows(BusinessException.class, () -> service.block(3L));

        assertEquals(CardStatus.CANCELLED, card.getStatus());
    }

    @Test
    void block_shouldThrowNotFound_whenCardIsNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(cardRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.block(3L));
    }

    @Test
    void unblock_shouldActivateCard_whenAccountIsActive() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.BLOCKED, AccountStatus.ACTIVE);
        when(cardRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.of(card));
        when(transactionRepository.sumCardSpendingSince(eq(3L), any())).thenReturn(BigDecimal.ZERO);
        when(cardMapper.toResponse(card, BigDecimal.ZERO)).thenReturn(CardResponse.builder().id(3L).build());

        service.unblock(3L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
    }

    @Test
    void unblock_shouldThrowBusinessException_whenAccountIsFrozen() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.BLOCKED, AccountStatus.FROZEN);
        when(cardRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.of(card));

        assertThrows(BusinessException.class, () -> service.unblock(3L));

        assertEquals(CardStatus.BLOCKED, card.getStatus());
    }

    @Test
    void updateLimits_shouldSetDailyLimitAndOnlinePayment() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        when(cardRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.of(card));
        when(transactionRepository.sumCardSpendingSince(eq(3L), any())).thenReturn(BigDecimal.ZERO);
        when(cardMapper.toResponse(card, BigDecimal.ZERO)).thenReturn(CardResponse.builder().id(3L).build());

        UpdateCardLimitsRequest req = UpdateCardLimitsRequest.builder()
                .dailyLimit(new BigDecimal("50000.00"))
                .onlinePaymentEnabled(false)
                .build();

        service.updateLimits(3L, req);

        assertEquals(new BigDecimal("50000.00"), card.getDailyLimit());
        assertFalse(card.isOnlinePaymentEnabled());
    }

    @Test
    void pay_shouldDebitAccountWithCard_andSaveCategory() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        Account account = card.getAccount();
        when(cardRepository.findAccountIdByIdAndUserId(3L, 7L)).thenReturn(Optional.of(1L));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(card));
        when(transactionRepository.sumCardSpendingSince(eq(3L), any())).thenReturn(new BigDecimal("10000.00"));

        Category food = Category.builder().id(1L).name("Élelmiszer").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));

        Transaction tx = Transaction.builder().id(50L).build();
        when(ledgerService.debit(account, TransactionType.OUTCOME, new BigDecimal("8990.00"), "Tesco", "Kártyás fizetés", null, card)).thenReturn(tx);

        TransactionCategory link = TransactionCategory.builder().transaction(tx).category(food).build();
        when(transactionCategoryMapper.toEntity(tx, food)).thenReturn(link);

        CategoryResponse foodResp = CategoryResponse.builder().id(1L).name("Élelmiszer").build();
        when(categoryMapper.toResponse(food)).thenReturn(foodResp);

        TransactionResponse expected = TransactionResponse.builder().id(50L).cardId(3L).build();
        when(transactionMapper.toResponse(tx, List.of(foodResp))).thenReturn(expected);

        CardPaymentRequest req = CardPaymentRequest.builder()
                .merchantName("Tesco")
                .amount(new BigDecimal("8990.00"))
                .categoryId(1L)
                .online(false)
                .build();

        TransactionResponse resp = service.pay(3L, req);

        assertEquals(expected, resp);
        verify(transactionCategoryRepository).save(link);
    }

    @Test
    void pay_shouldLockAccountBeforeLoadingCard() {
        when(currentUser.requireId()).thenReturn(7L);

        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        when(cardRepository.findAccountIdByIdAndUserId(3L, 7L)).thenReturn(Optional.of(1L));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(card.getAccount()));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(card));
        when(transactionRepository.sumCardSpendingSince(eq(3L), any())).thenReturn(BigDecimal.ZERO);

        CardPaymentRequest req = CardPaymentRequest.builder().merchantName("MOL").amount(BigDecimal.TEN).build();

        service.pay(3L, req);

        InOrder inOrder = inOrder(accountRepository, cardRepository);
        inOrder.verify(accountRepository).findByIdForUpdate(1L);
        inOrder.verify(cardRepository).findById(3L);
    }

    @Test
    void pay_shouldThrowNotFound_whenCardIsNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(cardRepository.findAccountIdByIdAndUserId(3L, 7L)).thenReturn(Optional.empty());

        CardPaymentRequest req = CardPaymentRequest.builder().merchantName("Tesco").amount(BigDecimal.TEN).build();

        assertThrows(NotFoundException.class, () -> service.pay(3L, req));

        verifyNoInteractions(accountRepository, ledgerService);
    }

    @Test
    void pay_shouldThrowBusinessException_whenCardIsBlocked() {
        Card card = card(3L, CardStatus.BLOCKED, AccountStatus.ACTIVE);
        prepareLockedPayment(card);

        CardPaymentRequest req = CardPaymentRequest.builder().merchantName("Tesco").amount(BigDecimal.TEN).build();

        assertThrows(BusinessException.class, () -> service.pay(3L, req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void pay_shouldThrowBusinessException_whenCardIsExpired() {
        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        card.setExpiryDate(DateUtils.today().minusDays(1));
        prepareLockedPayment(card);

        CardPaymentRequest req = CardPaymentRequest.builder().merchantName("Tesco").amount(BigDecimal.TEN).build();

        assertThrows(BusinessException.class, () -> service.pay(3L, req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void pay_shouldThrowBusinessException_whenOnlinePaymentIsDisabled() {
        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        card.setOnlinePaymentEnabled(false);
        prepareLockedPayment(card);

        CardPaymentRequest req = CardPaymentRequest.builder().merchantName("Amazon.de").amount(BigDecimal.TEN).online(true).build();

        assertThrows(BusinessException.class, () -> service.pay(3L, req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void pay_shouldThrowBusinessException_whenDailyLimitWouldBeExceeded() {
        Card card = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        prepareLockedPayment(card);

        // A limit 200 000 Ft, ma már 195 000 Ft-ot költött
        when(transactionRepository.sumCardSpendingSince(eq(3L), any())).thenReturn(new BigDecimal("195000.00"));

        CardPaymentRequest req = CardPaymentRequest.builder().merchantName("MediaMarkt").amount(new BigDecimal("5000.01")).build();

        assertThrows(BusinessException.class, () -> service.pay(3L, req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void createCard_shouldCreateValidCard_withHufDefaultLimit() {
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = User.builder().id(7L).firstName("Anna").lastName("Kovács").build();
        Account account = Account.builder().id(1L).user(user).currency("HUF").build();

        Card card = service.createCard(account);

        assertEquals(account, card.getAccount());
        assertEquals(16, card.getCardNumber().length());
        assertTrue(card.getCardNumber().startsWith("489512"));
        assertTrue(isLuhnValid(card.getCardNumber()));
        assertEquals("KOVÁCS ANNA", card.getHolderName());
        assertEquals(YearMonth.from(DateUtils.today()).plusYears(4).atEndOfMonth(), card.getExpiryDate());
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(new BigDecimal("200000"), card.getDailyLimit());
        assertTrue(card.isOnlinePaymentEnabled());
    }

    @Test
    void createCard_shouldUseForeignLimit_forForeignCurrencyAccount() {
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = Account.builder().id(2L).user(User.builder().firstName("Anna").lastName("Kovács").build()).currency("EUR").build();

        Card card = service.createCard(account);

        assertEquals(new BigDecimal("1000"), card.getDailyLimit());
    }

    @Test
    void createCard_shouldRetry_whenGeneratedNumberAlreadyExists() {
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(true, false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = Account.builder().id(1L).user(User.builder().firstName("Anna").lastName("Kovács").build()).currency("HUF").build();

        service.createCard(account);

        verify(cardRepository, times(2)).existsByCardNumber(anyString());
    }

    @Test
    void cancelCardsOfAccount_shouldCancelAllCards() {
        Card c1 = card(3L, CardStatus.ACTIVE, AccountStatus.ACTIVE);
        Card c2 = card(4L, CardStatus.BLOCKED, AccountStatus.ACTIVE);
        when(cardRepository.findByAccount_Id(1L)).thenReturn(List.of(c1, c2));

        service.cancelCardsOfAccount(1L);

        assertEquals(CardStatus.CANCELLED, c1.getStatus());
        assertEquals(CardStatus.CANCELLED, c2.getStatus());
    }

    private void prepareLockedPayment(Card card) {
        when(currentUser.requireId()).thenReturn(7L);
        when(cardRepository.findAccountIdByIdAndUserId(card.getId(), 7L)).thenReturn(Optional.of(card.getAccount().getId()));
        when(accountRepository.findByIdForUpdate(card.getAccount().getId())).thenReturn(Optional.of(card.getAccount()));
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
    }

    private Card card(Long id, CardStatus status, AccountStatus accountStatus) {
        Account account = Account.builder()
                .id(1L)
                .currency("HUF")
                .balance(new BigDecimal("100000.00"))
                .status(accountStatus)
                .build();

        return Card.builder()
                .id(id)
                .account(account)
                .cardNumber("4895127301601184")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(status)
                .dailyLimit(new BigDecimal("200000.00"))
                .onlinePaymentEnabled(true)
                .build();
    }

    // Luhn-ellenőrzés: jobbról minden második számjegy duplázva, az összeg 10-zel osztható
    private boolean isLuhnValid(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
