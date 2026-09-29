package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CreateStandingOrderRequest;
import hu.finex.main.dto.StandingOrderResponse;
import hu.finex.main.dto.UpdateStandingOrderRequest;
import hu.finex.main.model.Account;
import hu.finex.main.model.StandingOrder;

@Component
public class StandingOrderMapper {

    // A címzett számlaszáma már normalizálva érkezik
    public StandingOrder toEntity(CreateStandingOrderRequest request, Account account, String toAccountNumber) {
        return StandingOrder.builder()
                .account(account)
                .toAccountNumber(toAccountNumber)
                .partnerName(request.getPartnerName())
                .amount(request.getAmount())
                .message(request.getMessage())
                .frequency(request.getFrequency())
                .nextExecutionDate(request.getFirstExecutionDate())
                .active(true)
                .build();
    }

    public void updateEntity(StandingOrder order, UpdateStandingOrderRequest request) {
        order.setAmount(request.getAmount());
        order.setMessage(request.getMessage());
        order.setFrequency(request.getFrequency());
        order.setNextExecutionDate(request.getNextExecutionDate());
    }

    public StandingOrderResponse toResponse(StandingOrder order) {
        return StandingOrderResponse.builder()
                .id(order.getId())
                .accountId(order.getAccount().getId())
                .accountNumber(order.getAccount().getAccountNumber())
                .toAccountNumber(order.getToAccountNumber())
                .partnerName(order.getPartnerName())
                .amount(order.getAmount())
                .currency(order.getAccount().getCurrency())
                .message(order.getMessage())
                .frequency(order.getFrequency())
                .nextExecutionDate(order.getNextExecutionDate())
                .active(order.isActive())
                .lastExecutionAt(order.getLastExecutionAt())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
