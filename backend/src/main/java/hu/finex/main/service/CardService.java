package hu.finex.main.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.CardPaymentRequest;
import hu.finex.main.dto.CardResponse;
import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.UpdateCardLimitsRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.CardMapper;
import hu.finex.main.mapper.CategoryMapper;
import hu.finex.main.mapper.TransactionCategoryMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.Card;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
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
import hu.finex.main.util.MoneyUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final CardMapper cardMapper;
    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final TransactionCategoryMapper transactionCategoryMapper;
    private final LedgerService ledgerService;
    private final CurrentUser currentUser;
    private final FinexProperties finexProperties;

    @Transactional(readOnly = true)
    public List<CardResponse> listMine() {
        return cardRepository.findByAccount_User_IdOrderByCreatedAtAsc(currentUser.requireId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CardResponse getById(Long id) {
        return toResponse(findOwned(id));
    }

    // Ideiglenes letiltás (pl. ha elveszett a kártya), a felhasználó bármikor feloldhatja
    @Transactional
    public CardResponse block(Long id) {
        Card card = findOwned(id);
        ensureNotCancelled(card);

        card.setStatus(CardStatus.BLOCKED);

        return toResponse(card);
    }

    @Transactional
    public CardResponse unblock(Long id) {
        Card card = findOwned(id);
        ensureNotCancelled(card);

        if (card.getAccount().getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("A kártya nem oldható fel, mert a hozzá tartozó számla nem aktív.");
        }

        card.setStatus(CardStatus.ACTIVE);

        return toResponse(card);
    }

    @Transactional
    public CardResponse updateLimits(Long id, UpdateCardLimitsRequest request) {
        Card card = findOwned(id);
        ensureNotCancelled(card);

        card.setDailyLimit(request.getDailyLimit());
        card.setOnlinePaymentEnabled(request.getOnlinePaymentEnabled());

        return toResponse(card);
    }

    // Kártyás fizetés (demó): ellenőrzi a kártya és a számla állapotát, a lejáratot, az online fizetést és a napi limitet
    @Transactional
    public TransactionResponse pay(Long cardId, CardPaymentRequest request) {
        Long accountId = cardRepository.findAccountIdByIdAndUserId(cardId, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Kártya nem található."));

        // Előbb a számlát zároljuk, hogy a fedezet- és limitellenőrzés ne kerülhessen versenyhelyzetbe egy párhuzamos fizetéssel
        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(() -> new NotFoundException("Számla nem található."));
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Kártya nem található."));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException("A kártya le van tiltva, ezért nem használható.");
        }
        if (card.getExpiryDate().isBefore(DateUtils.today())) {
            throw new BusinessException("A kártya lejárt.");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("A számla nem aktív, ezért nem végezhető rajta művelet.");
        }
        if (request.isOnline() && !card.isOnlinePaymentEnabled()) {
            throw new BusinessException("Az online fizetés ezen a kártyán le van tiltva.");
        }

        // Napi limit: a mai kártyás költés és ez a fizetés együtt sem lépheti túl
        BigDecimal spentToday = transactionRepository.sumCardSpendingSince(card.getId(), DateUtils.startOfToday());
        if (spentToday.add(request.getAmount()).compareTo(card.getDailyLimit()) > 0) {
            throw new BusinessException("A fizetés túllépné a kártya napi limitjét (" + MoneyUtils.format(card.getDailyLimit(), account.getCurrency()) + ").");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new NotFoundException("Kategória nem található."));
        }

        String message = request.isOnline() ? "Online kártyás fizetés" : "Kártyás fizetés";
        Transaction transaction = ledgerService.debit(account, TransactionType.OUTCOME, request.getAmount(), request.getMerchantName(), message, null, card);

        List<CategoryResponse> categories = List.of();
        if (category != null) {
            transactionCategoryRepository.save(transactionCategoryMapper.toEntity(transaction, category));
            categories = List.of(categoryMapper.toResponse(category));
        }

        return transactionMapper.toResponse(transaction, categories);
    }

    // Új kártya egy számlához (számlanyitáskor): 4 évig érvényes, a lejárat a hónap utolsó napja
    @Transactional
    public Card createCard(Account account) {
        BigDecimal dailyLimit = "HUF".equals(account.getCurrency())
                ? finexProperties.getCardDefaultDailyLimit()
                : finexProperties.getCardDefaultDailyLimitForeign();

        Card card = Card.builder()
                .account(account)
                .cardNumber(generateUniqueCardNumber())
                .holderName(account.getUser().getFullName().toUpperCase(Locale.ROOT))
                .expiryDate(YearMonth.from(DateUtils.today()).plusYears(4).atEndOfMonth())
                .status(CardStatus.ACTIVE)
                .dailyLimit(dailyLimit)
                .onlinePaymentEnabled(true)
                .build();

        return cardRepository.save(card);
    }

    // Számla lezárásakor a kártyák véglegesen megszűnnek
    @Transactional
    public void cancelCardsOfAccount(Long accountId) {
        cardRepository.findByAccount_Id(accountId).forEach(card -> card.setStatus(CardStatus.CANCELLED));
    }

    // Admin: egy felhasználó kártyái
    @Transactional(readOnly = true)
    public List<CardResponse> listByUser(Long userId) {
        return cardRepository.findByAccount_User_IdOrderByCreatedAtAsc(userId).stream().map(this::toResponse).toList();
    }

    private Card findOwned(Long id) {
        return cardRepository.findByIdAndAccount_User_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Kártya nem található."));
    }

    private void ensureNotCancelled(Card card) {
        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new BusinessException("A megszűnt kártya nem módosítható.");
        }
    }

    private CardResponse toResponse(Card card) {
        BigDecimal spentToday = transactionRepository.sumCardSpendingSince(card.getId(), DateUtils.startOfToday());
        return cardMapper.toResponse(card, spentToday);
    }

    // Egyedi kártyaszám (ütközés esetén újrapróbálkozás)
    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
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
}
