package hu.finex.main.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.ExchangeRequest;
import hu.finex.main.dto.ExchangeResponse;
import hu.finex.main.dto.ReverseTransactionRequest;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.TransactionSearchRequest;
import hu.finex.main.dto.TransferRequest;
import hu.finex.main.dto.TransferResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.CategoryMapper;
import hu.finex.main.mapper.TransactionCategoryMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import hu.finex.main.util.IbanUtils;
import hu.finex.main.util.MoneyUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    // A devizaváltás tételeinek partnerneve
    public static final String EXCHANGE_PARTNER = "Devizaváltás";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionCategoryMapper transactionCategoryMapper;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;
    private final FinexProperties finexProperties;
    private final ExchangeRateService exchangeRateService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findByIdAndAccount_User_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Tranzakció nem található."));

        List<CategoryResponse> categories = loadCategories(List.of(transaction.getId())).getOrDefault(transaction.getId(), List.of());
        return transactionMapper.toResponse(transaction, categories, transactionRepository.existsByReversalOf_Id(transaction.getId()));
    }

    @Transactional(readOnly = true)
    public Page<TransactionListItemResponse> search(TransactionSearchRequest filter, Pageable pageable) {
        Long userId = currentUser.requireId();

        if (filter.getAccountId() != null && !accountRepository.existsByIdAndUser_Id(filter.getAccountId(), userId)) {
            throw new NotFoundException("Számla nem található.");
        }
        if (filter.getFrom() != null && filter.getTo() != null && filter.getFrom().isAfter(filter.getTo())) {
            throw new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál.");
        }

        Instant from = filter.getFrom() != null ? DateUtils.startOfDay(filter.getFrom()) : DateUtils.BEGINNING_OF_TIME;
        Instant to = filter.getTo() != null ? DateUtils.startOfDay(filter.getTo().plusDays(1)) : DateUtils.END_OF_TIME;
        String search = StringUtils.hasText(filter.getSearch()) ? "%" + filter.getSearch().trim().toLowerCase(Locale.ROOT) + "%" : null;

        Page<Transaction> page = transactionRepository.search(userId, filter.getAccountId(), filter.getType(), from, to,
                filter.getMinAmount(), filter.getMaxAmount(), search, filter.getCategoryId(), pageable);

        return toListItems(page);
    }

    // Admin: egy számla tételei (a sztornóhoz), legfrissebb elöl
    @Transactional(readOnly = true)
    public Page<TransactionListItemResponse> listByAccountForAdmin(Long accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Számla nem található.");
        }
        return toListItems(transactionRepository.findByAccount_IdOrderByCreatedAtDescIdDesc(accountId, pageable));
    }

    // A lap összes tételének kategóriái és sztornó-állapota egy-egy lekérdezéssel (nem tételenként)
    private Page<TransactionListItemResponse> toListItems(Page<Transaction> page) {
        List<Long> ids = page.getContent().stream().map(Transaction::getId).toList();
        Map<Long, List<CategoryResponse>> categories = loadCategories(ids);
        Set<Long> reversed = ids.isEmpty() ? Set.of() : Set.copyOf(transactionRepository.findReversedIds(ids));

        return page.map(transaction -> transactionMapper.toListItem(transaction, categories.getOrDefault(transaction.getId(), List.of()), reversed.contains(transaction.getId())));
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        User owner = currentUser.requireEntity();

        return executeTransfer(owner, request.getFromAccountId(), request.getToAccountNumber(), request.getPartnerName(),
                request.getAmount(), request.getMessage(), request.getCategoryIds());
    }

    // Az utalás magja (kézi és rendszeres utalás is); FineX-es címzettnél a jóváírás is lekönyvelődik
    @Transactional
    public TransferResponse executeTransfer(User owner, Long fromAccountId, String toAccountNumber, String partnerName,
                                            BigDecimal amount, String message, List<Long> categoryIds) {
        String targetNumber = IbanUtils.normalize(toAccountNumber);
        if (!IbanUtils.isValid(targetNumber)) {
            throw new BusinessException("Érvénytelen számlaszám (IBAN).");
        }
        if (!accountRepository.existsByIdAndUser_Id(fromAccountId, owner.getId())) {
            throw new NotFoundException("Forrás számla nem található.");
        }

        Long targetId = accountRepository.findIdByAccountNumber(targetNumber).orElse(null);
        if (fromAccountId.equals(targetId)) {
            throw new BusinessException("Nem utalhatsz ugyanarra a számlára.");
        }

        // Zárolás azonosító szerinti sorrendben, így két szembe menő utalás nem akadhat össze (holtpont)
        Account from;
        Account to = null;
        if (targetId != null && targetId < fromAccountId) {
            to = lock(targetId);
            from = lock(fromAccountId);
        } else {
            from = lock(fromAccountId);
            if (targetId != null) {
                to = lock(targetId);
            }
        }

        if (from.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("A forrás számla nem aktív, ezért nem indítható róla utalás.");
        }
        if (to != null) {
            if (to.getStatus() != AccountStatus.ACTIVE) {
                throw new BusinessException("A címzett számla jelenleg nem fogad jóváírást.");
            }
            if (!from.getCurrency().equals(to.getCurrency())) {
                throw new BusinessException("A két számla devizaneme eltér (" + from.getCurrency() + " / " + to.getCurrency() + "), ezért közvetlen utalás nem lehetséges.");
            }
        }

        // Napi limit: a forintos limitet devizaszámlán az árfolyamon számolt megfelelőjével vetjük össze
        BigDecimal sentToday = transactionRepository.sumAmountByAccountAndTypeSince(from.getId(), TransactionType.TRANSFER_OUT, DateUtils.startOfToday());
        if (exchangeRateService.toHuf(sentToday.add(amount), from.getCurrency()).compareTo(finexProperties.getTransferDailyLimit()) > 0) {
            BigDecimal limit = exchangeRateService.fromHuf(finexProperties.getTransferDailyLimit(), from.getCurrency());
            throw new BusinessException("Az utalás túllépné a napi utalási limitet (" + MoneyUtils.format(limit, from.getCurrency()) + ").");
        }

        List<Category> categories = findCategories(categoryIds);

        // A fedezetet a LedgerService ellenőrzi a terheléskor
        Transaction outgoing = ledgerService.debit(from, TransactionType.TRANSFER_OUT, amount, partnerName, message, targetNumber, null);
        saveCategories(outgoing, categories);

        if (to != null) {
            ledgerService.creditLinked(to, TransactionType.TRANSFER_IN, amount, owner.getFullName(), message, from.getAccountNumber(), outgoing);

            // Saját számlák közötti átvezetésről nem kell értesítés
            if (!to.getUser().getId().equals(owner.getId())) {
                notificationService.notify(to.getUser(), NotificationType.TRANSACTION, "Beérkező utalás",
                        MoneyUtils.format(amount, to.getCurrency()) + " érkezett " + owner.getFullName() + " számlájáról.");
            }
        }

        return TransferResponse.builder()
                .transactionId(outgoing.getId())
                .fromAccountId(from.getId())
                .toAccountNumber(targetNumber)
                .partnerName(partnerName)
                .amount(amount)
                .currency(from.getCurrency())
                .message(message)
                .categories(categories.stream().map(categoryMapper::toResponse).toList())
                .fromAccountNewBalance(from.getBalance())
                .internal(to != null)
                .createdAt(outgoing.getCreatedAt())
                .build();
    }

    // Devizaváltás saját számlák között a bank árfolyamán (a napi utalási limit is vonatkozik rá)
    @Transactional
    public ExchangeResponse exchange(ExchangeRequest request) {
        User owner = currentUser.requireEntity();

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BusinessException("A forrás- és a célszámla nem lehet ugyanaz.");
        }
        if (!accountRepository.existsByIdAndUser_Id(request.getFromAccountId(), owner.getId())
                || !accountRepository.existsByIdAndUser_Id(request.getToAccountId(), owner.getId())) {
            throw new NotFoundException("Számla nem található.");
        }

        // Zárolás azonosító szerinti sorrendben (holtpont ellen, mint az utalásnál)
        Account from;
        Account to;
        if (request.getToAccountId() < request.getFromAccountId()) {
            to = lock(request.getToAccountId());
            from = lock(request.getFromAccountId());
        } else {
            from = lock(request.getFromAccountId());
            to = lock(request.getToAccountId());
        }

        if (from.getStatus() != AccountStatus.ACTIVE || to.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Csak aktív számlák között lehet váltani.");
        }
        if (from.getCurrency().equals(to.getCurrency())) {
            throw new BusinessException("A két számla devizaneme azonos: ehhez nem kell váltás, használd az utalást.");
        }

        BigDecimal amount = request.getAmount();
        BigDecimal sentToday = transactionRepository.sumAmountByAccountAndTypeSince(from.getId(), TransactionType.TRANSFER_OUT, DateUtils.startOfToday());
        if (exchangeRateService.toHuf(sentToday.add(amount), from.getCurrency()).compareTo(finexProperties.getTransferDailyLimit()) > 0) {
            BigDecimal limit = exchangeRateService.fromHuf(finexProperties.getTransferDailyLimit(), from.getCurrency());
            throw new BusinessException("A váltás túllépné a napi limitet (" + MoneyUtils.format(limit, from.getCurrency()) + ").");
        }

        BigDecimal converted = exchangeRateService.convert(amount, from.getCurrency(), to.getCurrency());
        if (converted.signum() <= 0) {
            throw new BusinessException("Az összeg túl kicsi a váltáshoz.");
        }

        String message = "Váltás: " + MoneyUtils.format(amount, from.getCurrency()) + " → " + MoneyUtils.format(converted, to.getCurrency());
        Transaction outgoing = ledgerService.debit(from, TransactionType.TRANSFER_OUT, amount, EXCHANGE_PARTNER, message, to.getAccountNumber(), null);
        ledgerService.creditLinked(to, TransactionType.TRANSFER_IN, converted, EXCHANGE_PARTNER, message, from.getAccountNumber(), outgoing);

        return ExchangeResponse.builder()
                .transactionId(outgoing.getId())
                .fromAccountId(from.getId())
                .toAccountId(to.getId())
                .fromAmount(amount)
                .fromCurrency(from.getCurrency())
                .toAmount(converted)
                .toCurrency(to.getCurrency())
                .rate(exchangeRateService.crossRate(from.getCurrency(), to.getCurrency()))
                .fromAccountNewBalance(from.getBalance())
                .toAccountNewBalance(to.getBalance())
                .createdAt(outgoing.getCreatedAt())
                .build();
    }

    // Admin: sztornó ellentétes irányú javító tétellel; belső utalásnál a pár másik fele is sztornózódik
    @Transactional
    public TransactionResponse reverse(Long transactionId, ReverseTransactionRequest request) {
        Transaction original = transactionRepository.findById(transactionId).orElseThrow(() -> new NotFoundException("Tranzakció nem található."));

        if (original.getReversalOf() != null) {
            throw new BusinessException("Sztornó tétel nem sztornózható.");
        }
        if (transactionRepository.existsByReversalOf_Id(transactionId)) {
            throw new BusinessException("Ezt a tételt már sztornózták.");
        }

        boolean cardPayment = original.getType() == TransactionType.OUTCOME && original.getCard() != null;
        boolean cashDeposit = original.getType() == TransactionType.INCOME && AccountService.CASH_DEPOSIT_PARTNER.equals(original.getPartnerName());
        boolean transferOut = original.getType() == TransactionType.TRANSFER_OUT;
        if (!cardPayment && !cashDeposit && !transferOut) {
            throw new BusinessException("Csak kártyás fizetés, készpénzbefizetés és kimenő utalás sztornózható.");
        }

        Transaction counterpart = transferOut ? transactionRepository.findFirstByRelatedTransaction_Id(transactionId).orElse(null) : null;
        if (transferOut && counterpart == null && accountRepository.findIdByAccountNumber(original.getToAccount()).isPresent()) {
            throw new BusinessException("Ez a belső utalás a tételpárosítás bevezetése előtt készült, ezért nem sztornózható automatikusan.");
        }

        // Zárolás azonosító szerinti sorrendben
        Long originalAccountId = original.getAccount().getId();
        Long counterpartAccountId = counterpart != null ? counterpart.getAccount().getId() : null;
        Account originalAccount;
        Account counterpartAccount = null;
        if (counterpartAccountId != null && counterpartAccountId < originalAccountId) {
            counterpartAccount = lock(counterpartAccountId);
            originalAccount = lock(originalAccountId);
        } else {
            originalAccount = lock(originalAccountId);
            if (counterpartAccountId != null) {
                counterpartAccount = lock(counterpartAccountId);
            }
        }

        // Előbb a címzett oldal (terhelés): ha nincs rajta fedezet, semmi nem könyvelődik
        if (counterpart != null) {
            ledgerService.reverse(counterpartAccount, counterpart, request.getReason());
            notificationService.notify(counterpartAccount.getUser(), NotificationType.TRANSACTION, "Sztornózott tétel",
                    MoneyUtils.format(counterpart.getAmount(), counterpart.getCurrency()) + " jóváírást sztornóztunk a(z) " + counterpartAccount.getName() + " számládon. Ok: " + request.getReason());
        }
        Transaction reversal = ledgerService.reverse(originalAccount, original, request.getReason());
        notificationService.notify(originalAccount.getUser(), NotificationType.TRANSACTION, "Sztornózott tétel",
                MoneyUtils.format(original.getAmount(), original.getCurrency()) + " összegű tételt sztornóztunk a(z) " + originalAccount.getName() + " számládon. Ok: " + request.getReason());

        auditService.record(AuditAction.TRANSACTION_REVERSED, AuditTargetType.TRANSACTION, original.getId(),
                originalAccount.getAccountNumber() + ": " + MoneyUtils.format(original.getAmount(), original.getCurrency())
                + " (" + original.getType() + ") – " + request.getReason());

        return transactionMapper.toResponse(reversal);
    }

    private Account lock(Long accountId) {
        return accountRepository.findByIdForUpdate(accountId).orElseThrow(() -> new NotFoundException("Számla nem található."));
    }

    // A megadott kategóriák betöltése, egy hiányzó azonosító esetén hiba
    private List<Category> findCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        Set<Long> distinctIds = new LinkedHashSet<>(categoryIds);
        List<Category> categories = categoryRepository.findAllById(distinctIds);

        if (categories.size() != distinctIds.size()) {
            throw new NotFoundException("Kategória nem található.");
        }
        return categories;
    }

    private void saveCategories(Transaction transaction, List<Category> categories) {
        for (Category category : categories) {
            transactionCategoryRepository.save(transactionCategoryMapper.toEntity(transaction, category));
        }
    }

    // Tranzakció azonosító -> a tranzakció kategóriái
    private Map<Long, List<CategoryResponse>> loadCategories(List<Long> transactionIds) {
        if (transactionIds.isEmpty()) {
            return Map.of();
        }

        return transactionCategoryRepository.findByTransaction_IdIn(transactionIds).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getTransaction().getId(),
                        Collectors.mapping(link -> categoryMapper.toResponse(link.getCategory()), Collectors.toList())));
    }
}
