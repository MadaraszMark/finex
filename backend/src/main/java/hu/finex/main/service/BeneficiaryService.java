package hu.finex.main.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.BeneficiaryResponse;
import hu.finex.main.dto.CreateBeneficiaryRequest;
import hu.finex.main.dto.UpdateBeneficiaryRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BeneficiaryMapper;
import hu.finex.main.model.Beneficiary;
import hu.finex.main.model.User;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BeneficiaryRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.IbanUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryMapper beneficiaryMapper;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> listMine() {
        return beneficiaryRepository.findByUser_IdOrderByNameAsc(currentUser.requireId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BeneficiaryResponse create(CreateBeneficiaryRequest request) {
        User user = currentUser.requireEntity();
        String accountNumber = validateAccountNumber(request.getAccountNumber());

        if (beneficiaryRepository.existsByUser_IdAndAccountNumber(user.getId(), accountNumber)) {
            throw new BusinessException("Ez a számlaszám már szerepel a kedvezményezettjeid között.");
        }

        Beneficiary beneficiary = beneficiaryMapper.toEntity(request, user, accountNumber);
        beneficiary = beneficiaryRepository.save(beneficiary);

        return toResponse(beneficiary);
    }

    @Transactional
    public BeneficiaryResponse update(Long id, UpdateBeneficiaryRequest request) {
        Beneficiary beneficiary = findOwned(id);
        String accountNumber = validateAccountNumber(request.getAccountNumber());

        if (beneficiaryRepository.existsByUser_IdAndAccountNumberAndIdNot(beneficiary.getUser().getId(), accountNumber, id)) {
            throw new BusinessException("Ez a számlaszám már szerepel a kedvezményezettjeid között.");
        }

        beneficiaryMapper.updateEntity(beneficiary, request, accountNumber);
        return toResponse(beneficiary);
    }

    @Transactional
    public void delete(Long id) {
        beneficiaryRepository.delete(findOwned(id));
    }

    private Beneficiary findOwned(Long id) {
        return beneficiaryRepository.findByIdAndUser_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Kedvezményezett nem található."));
    }

    // Szóközök nélküli, nagybetűs alakban tároljuk, és csak érvényes IBAN menthető
    private String validateAccountNumber(String accountNumber) {
        String normalized = IbanUtils.normalize(accountNumber);
        if (!IbanUtils.isValid(normalized)) {
            throw new BusinessException("Érvénytelen számlaszám (IBAN).");
        }
        return normalized;
    }

    // A frontend jelezheti, ha a kedvezményezett FineX-es (az utalás ilyenkor azonnal jóváíródik)
    private BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        boolean internal = accountRepository.existsByAccountNumber(beneficiary.getAccountNumber());
        return beneficiaryMapper.toResponse(beneficiary, internal);
    }
}
