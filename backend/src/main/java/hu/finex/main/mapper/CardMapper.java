package hu.finex.main.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CardResponse;
import hu.finex.main.model.Card;

@Component
public class CardMapper {

    public CardResponse toResponse(Card card, BigDecimal spentToday) {
        return CardResponse.builder()
                .id(card.getId())
                .accountId(card.getAccount().getId())
                .accountNumber(card.getAccount().getAccountNumber())
                .maskedNumber(maskCardNumber(card.getCardNumber()))
                .holderName(card.getHolderName())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .dailyLimit(card.getDailyLimit())
                .spentToday(spentToday)
                .onlinePaymentEnabled(card.isOnlinePaymentEnabled())
                .createdAt(card.getCreatedAt())
                .build();
    }

    // A teljes kártyaszám soha nem megy ki az API-n, csak az utolsó 4 számjegy
    public String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
