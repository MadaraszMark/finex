package hu.finex.main.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.CreateTransactionRequest;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.TransactionCategory;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BalanceHistoryRepository;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import hu.finex.main.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

    private final TransactionMapper transactionMapper;
    private final BalanceHistoryMapper balanceHistoryMapper;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {

        Account account = accountRepository.findById(request.getAccountId()).orElseThrow(() -> new NotFoundException("Számla nem található."));

        if (!account.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new BusinessException("A tranzakció devizaneme nem egyezik a számla devizanemével.");
        }

        BigDecimal amount = request.getAmount();

        if (request.getType() == TransactionType.INCOME || request.getType() == TransactionType.TRANSFER_IN) {
            account.setBalance(account.getBalance().add(amount));
        } else if (request.getType() == TransactionType.OUTCOME || request.getType() == TransactionType.TRANSFER_OUT) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("Nincs elegendő fedezet a tranzakcióhoz.");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            throw new BusinessException("Ismeretlen tranzakció típus.");
        }

        BalanceHistory history = balanceHistoryMapper.toEntity(account, account.getBalance());
        balanceHistoryRepository.save(history);

        Transaction transaction = transactionMapper.toEntity(request, account);
        transaction = transactionRepository.save(transaction);

        saveCategories(transaction, request.getCategoryIds());
        
        return buildResponseWithCategories(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new NotFoundException("Tranzakció nem található."));
        return buildResponseWithCategories(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionListItemResponse> listByAccount(Long accountId, Pageable pageable) {
        accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Számla nem található."));

        return transactionRepository.findByAccount_IdOrderByCreatedAtDesc(accountId, pageable).map(transactionMapper::toListItem);
    }
    
    private TransactionResponse buildResponseWithCategories(Transaction transaction) {

        // Kapcsoló rekordok
        List<TransactionCategory> links =
                transactionCategoryRepository.findByTransaction_Id(transaction.getId());

        List<CategoryResponse> categoryResponses = links.stream().map(link -> {
                    Category c = link.getCategory();
                    return CategoryResponse.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .icon(c.getIcon())
                            .build();
                })
                .toList();

        TransactionResponse response = transactionMapper.toResponse(transaction);

        response.setCategories(categoryResponses);

        return response;
    }
    
    private void saveCategories(Transaction transaction, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        // Ellenőrzés, hogy minden kategória létezik
        for (Long categoryId : categoryIds) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new NotFoundException("Kategória nem található: " + categoryId);
            }
        }

        // Kapcsoló rekordok mentése
        for (Long categoryId : categoryIds) {
            TransactionCategory tc = TransactionCategory.builder()
                    .transaction(transaction)
                    .category(Category.builder().id(categoryId).build())
                    .build();

            transactionCategoryRepository.save(tc);
        }
    }

}
