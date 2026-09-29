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

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findByIdAndAccount_User_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Tranzakció nem található."));

        List<CategoryResponse> categories = loadCategories(List.of(transaction.getId())).getOrDefault(transaction.getId(), List.of());
        return transactionMapper.toResponse(transaction, categories);
    }

    // A bejelentkezett felhasználó tételeinek keresése szűrőkkel, lapozva
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

        // A lap összes tételének kategóriái egyetlen lekérdezéssel
        Map<Long, List<CategoryResponse>> categories = loadCategories(page.getContent().stream().map(Transaction::getId).toList());

        return page.map(transaction -> transactionMapper.toListItem(transaction, categories.getOrDefault(transaction.getId(), List.of())));
    }

    // Utalás a bejelentkezett felhasználó számlájáról
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        User owner = currentUser.requireEntity();

        return executeTransfer(owner, request.getFromAccountId(), request.getToAccountNumber(), request.getPartnerName(),
                request.getAmount(), request.getMessage(), request.getCategoryIds());
    }

    // Az utalás magja: a kézi utalás és a rendszeres átutalás ütemezője is ezt használja.
    // FineX-es címzettnél mindkét oldalon könyvel (TRANSFER_OUT + TRANSFER_IN), külső számlánál csak a terhelés történik meg.
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

        // Zárolás mindig azonos (azonosító szerinti) sorrendben: két egymással szembe menő utalás így nem akadhat össze (holtpont)
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

        // Napi limit: a mai kimenő utalások és ez az utalás együtt sem lépheti túl
        BigDecimal sentToday = transactionRepository.sumAmountByAccountAndTypeSince(from.getId(), TransactionType.TRANSFER_OUT, DateUtils.startOfToday());
        if (sentToday.add(amount).compareTo(finexProperties.getTransferDailyLimit()) > 0) {
            throw new BusinessException("Az utalás túllépné a napi utalási limitet (" + MoneyUtils.format(finexProperties.getTransferDailyLimit(), from.getCurrency()) + ").");
        }

        List<Category> categories = findCategories(categoryIds);

        // A fedezetet a LedgerService ellenőrzi a terheléskor
        Transaction outgoing = ledgerService.debit(from, TransactionType.TRANSFER_OUT, amount, partnerName, message, targetNumber, null);
        saveCategories(outgoing, categories);

        if (to != null) {
            ledgerService.credit(to, TransactionType.TRANSFER_IN, amount, owner.getFullName(), message, from.getAccountNumber());

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
