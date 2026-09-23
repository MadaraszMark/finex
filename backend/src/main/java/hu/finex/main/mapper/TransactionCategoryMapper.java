package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.TransactionCategoryListItemResponse;
import hu.finex.main.dto.TransactionCategoryResponse;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.TransactionCategory;

@Component
public class TransactionCategoryMapper {

    public TransactionCategory toEntity(Transaction transaction, Category category) {
        return TransactionCategory.builder()
                .transaction(transaction)
                .category(category)
                .build();
    }

    public TransactionCategoryResponse toResponse(TransactionCategory tc) {
        return TransactionCategoryResponse.builder()
                .id(tc.getId())
                .transactionId(tc.getTransaction().getId())
                .categoryId(tc.getCategory().getId())
                .categoryName(tc.getCategory().getName())
                .categoryIcon(tc.getCategory().getIcon())
                .build();
    }

    public TransactionCategoryListItemResponse toListItem(TransactionCategory tc) {
        return TransactionCategoryListItemResponse.builder()
                .categoryId(tc.getCategory().getId())
                .categoryName(tc.getCategory().getName())
                .categoryIcon(tc.getCategory().getIcon())
                .build();
    }
}
