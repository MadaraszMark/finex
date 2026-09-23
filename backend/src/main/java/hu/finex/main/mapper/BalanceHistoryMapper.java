package hu.finex.main.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.dto.BalanceHistoryResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;

@Component
public class BalanceHistoryMapper {

    public BalanceHistory toEntity(Account account, BigDecimal balance) {
        return BalanceHistory.builder()
                .account(account)
                .balance(balance)
                .build();
    }

    public BalanceHistoryResponse toResponse(BalanceHistory history) {
        return BalanceHistoryResponse.builder()
                .id(history.getId())
                .accountId(history.getAccount().getId())
                .balance(history.getBalance())
                .createdAt(history.getCreatedAt())
                .build();
    }

    public BalanceHistoryListItemResponse toListItem(BalanceHistory history) {
        return BalanceHistoryListItemResponse.builder()
                .balance(history.getBalance())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
