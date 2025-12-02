package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CreateTransactionRequest;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.Transaction;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request, Account account) {
        return Transaction.builder()
                .account(account)
                .type(request.getType())
                .amount(request.getAmount())
                .message(request.getMessage())
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .currency(request.getCurrency())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .message(transaction.getMessage())
                .fromAccount(transaction.getFromAccount())
                .toAccount(transaction.getToAccount())
                .currency(transaction.getCurrency())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public TransactionListItemResponse toListItem(Transaction transaction) {
        return TransactionListItemResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .message(transaction.getMessage())
                .currency(transaction.getCurrency())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

