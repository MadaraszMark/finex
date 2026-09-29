package hu.finex.main.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.SavingsTransactionResponse;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.SavingsTransaction;
import hu.finex.main.model.enums.SavingsTransactionType;

@Component
public class SavingsTransactionMapper {

    public SavingsTransaction toEntity(SavingsAccount savingsAccount, SavingsTransactionType type, BigDecimal amount) {
        return SavingsTransaction.builder()
                .savingsAccount(savingsAccount)
                .type(type)
                .amount(amount)
                .balanceAfter(savingsAccount.getBalance())
                .build();
    }

    public SavingsTransactionResponse toResponse(SavingsTransaction transaction) {
        return SavingsTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
