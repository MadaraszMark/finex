package hu.finex.main.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.TransactionCategoryListItemResponse;
import hu.finex.main.dto.TransactionCategoryResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.TransactionCategoryMapper;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.TransactionCategory;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.security.CurrentUser;
import lombok.RequiredArgsConstructor;

// A tranzakciók kategorizálása: a felhasználó csak a saját tranzakcióihoz rendelhet kategóriát

@Service
@RequiredArgsConstructor
public class TransactionCategoryService {

    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionCategoryMapper transactionCategoryMapper;
    private final CurrentUser currentUser;

    @Transactional
    public TransactionCategoryResponse assignCategory(Long transactionId, Long categoryId) {
        Transaction transaction = transactionRepository.findByIdAndAccount_User_Id(transactionId, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Tranzakció nem található."));
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Kategória nem található."));

        boolean exists = transactionCategoryRepository.existsByTransaction_IdAndCategory_Id(transactionId, categoryId);

        if (exists) {
            throw new BusinessException("A kategória már hozzá van rendelve a tranzakcióhoz.");
        }

        TransactionCategory link = transactionCategoryMapper.toEntity(transaction, category);
        link = transactionCategoryRepository.save(link);

        return transactionCategoryMapper.toResponse(link);
    }

    @Transactional(readOnly = true)
    public List<TransactionCategoryListItemResponse> listByTransaction(Long transactionId) {
        if (!transactionRepository.existsByIdAndAccount_User_Id(transactionId, currentUser.requireId())) {
            throw new NotFoundException("Tranzakció nem található.");
        }

        List<TransactionCategory> items =transactionCategoryRepository.findByTransaction_Id(transactionId);

        return items.stream().map(transactionCategoryMapper::toListItem).toList();
    }

    // Csak a kapcsolat törlődik, maga a (könyvelt) tranzakció nem
    @Transactional
    public void deleteRelation(Long id) {
        TransactionCategory link = transactionCategoryRepository.findByIdAndTransaction_Account_User_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Kapcsolat nem található."));

        transactionCategoryRepository.delete(link);
    }
}
