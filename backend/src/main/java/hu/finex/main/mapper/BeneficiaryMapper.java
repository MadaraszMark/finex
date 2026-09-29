package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.BeneficiaryResponse;
import hu.finex.main.dto.CreateBeneficiaryRequest;
import hu.finex.main.dto.UpdateBeneficiaryRequest;
import hu.finex.main.model.Beneficiary;
import hu.finex.main.model.User;

@Component
public class BeneficiaryMapper {

    // A számlaszám már normalizálva érkezik (szóközök nélkül, nagybetűvel)
    public Beneficiary toEntity(CreateBeneficiaryRequest request, User user, String accountNumber) {
        return Beneficiary.builder()
                .user(user)
                .name(request.getName())
                .accountNumber(accountNumber)
                .note(request.getNote())
                .build();
    }

    public void updateEntity(Beneficiary beneficiary, UpdateBeneficiaryRequest request, String accountNumber) {
        beneficiary.setName(request.getName());
        beneficiary.setAccountNumber(accountNumber);
        beneficiary.setNote(request.getNote());
    }

    public BeneficiaryResponse toResponse(Beneficiary beneficiary, boolean internal) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .name(beneficiary.getName())
                .accountNumber(beneficiary.getAccountNumber())
                .note(beneficiary.getNote())
                .internal(internal)
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}
