package hu.finex.main.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.SavingsStatus;

@Component
public class SavingsAccountMapper {

    // Az egyenleg 0-ról indul, a kezdő összeg rendes befizetésként kerül rá (így a mozgások között is megjelenik)
    public SavingsAccount toEntity(CreateSavingsAccountRequest request, User user, String currency, BigDecimal interestRate) {
        return SavingsAccount.builder()
                .user(user)
                .name(request.getName())
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .interestRate(interestRate)
                .targetAmount(request.getTargetAmount())
                .status(SavingsStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void updateEntity(SavingsAccount entity, UpdateSavingsAccountRequest request) {
        entity.setName(request.getName());
        entity.setTargetAmount(request.getTargetAmount());
        entity.setUpdatedAt(Instant.now());
    }

    public SavingsAccountResponse toResponse(SavingsAccount entity) {
        return SavingsAccountResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .name(entity.getName())
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .interestRate(entity.getInterestRate())
                .targetAmount(entity.getTargetAmount())
                .progressPercent(progressPercent(entity))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // A célösszeg hány százaléka van meg (célösszeg nélkül null)
    private BigDecimal progressPercent(SavingsAccount entity) {
        if (entity.getTargetAmount() == null || entity.getTargetAmount().signum() <= 0) {
            return null;
        }
        return entity.getBalance()
                .multiply(BigDecimal.valueOf(100))
                .divide(entity.getTargetAmount(), 2, RoundingMode.HALF_UP);
    }
}
