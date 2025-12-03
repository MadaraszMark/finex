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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionCategoryService {

    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionCategoryMapper transactionCategoryMapper;

    @Transactional
    public TransactionCategoryResponse assignCategory(Long transactionId, Long categoryId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new NotFoundException("Tranzakció nem található."));
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
        if (!transactionRepository.existsById(transactionId)) {
            throw new NotFoundException("Tranzakció nem található.");
        }

        List<TransactionCategory> items =transactionCategoryRepository.findByTransaction_Id(transactionId);

        return items.stream().map(transactionCategoryMapper::toListItem).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionCategoryResponse> listByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Kategória nem található.");
        }

        List<TransactionCategory> items =transactionCategoryRepository.findByCategory_Id(categoryId);

        return items.stream().map(transactionCategoryMapper::toResponse).toList();
    }

    @Transactional
    public void deleteRelation(Long id) {
        TransactionCategory link = transactionCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Kapcsolat nem található."));

        transactionCategoryRepository.delete(link);
    }
}
