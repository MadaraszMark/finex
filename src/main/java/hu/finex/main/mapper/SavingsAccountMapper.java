package hu.finex.main.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.User;

@Component
public class SavingsAccountMapper {

    public SavingsAccount toEntity(CreateSavingsAccountRequest request, User user) {
        return SavingsAccount.builder()
                .user(user)
                .name(request.getName())
                .balance(request.getInitialBalance())
                .currency(request.getCurrency())
                .interestRate(request.getInterestRate())
                .status(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void updateEntity(SavingsAccount entity, UpdateSavingsAccountRequest request) {
        entity.setName(request.getName());
        entity.setInterestRate(request.getInterestRate());
        entity.setStatus(request.getStatus());
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
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
