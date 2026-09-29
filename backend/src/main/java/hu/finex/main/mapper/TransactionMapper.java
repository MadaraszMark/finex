package hu.finex.main.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.model.Transaction;

// A tranzakciók létrehozása a LedgerService-ben történik (könyvelés), itt csak a kifelé menő nézet készül

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return toResponse(transaction, List.of());
    }

    public TransactionResponse toResponse(Transaction transaction, List<CategoryResponse> categories) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .cardId(transaction.getCard() != null ? transaction.getCard().getId() : null)
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .message(transaction.getMessage())
                .partnerName(transaction.getPartnerName())
                .fromAccount(transaction.getFromAccount())
                .toAccount(transaction.getToAccount())
                .categories(categories)
                .currency(transaction.getCurrency())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public TransactionListItemResponse toListItem(Transaction transaction) {
        return toListItem(transaction, List.of());
    }

    public TransactionListItemResponse toListItem(Transaction transaction, List<CategoryResponse> categories) {
        return TransactionListItemResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .message(transaction.getMessage())
                .partnerName(transaction.getPartnerName())
                .currency(transaction.getCurrency())
                .categories(categories)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
