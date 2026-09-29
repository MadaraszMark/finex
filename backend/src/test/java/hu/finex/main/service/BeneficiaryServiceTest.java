package hu.finex.main.service;

import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BeneficiaryMapper;
import hu.finex.main.model.Beneficiary;
import hu.finex.main.model.User;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BeneficiaryRepository;
import hu.finex.main.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    private static final String BENCE_IBAN = "HU28104000950000521700000003";
    private static final String LANDLORD_IBAN = "HU66109180010000048900240017";

    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BeneficiaryMapper beneficiaryMapper;
    @Mock private CurrentUser currentUser;

    @InjectMocks private BeneficiaryService service;

    @Test
    void listMine_shouldMarkFinexAccountsAsInternal() {
        when(currentUser.requireId()).thenReturn(7L);

        Beneficiary bence = Beneficiary.builder().id(1L).accountNumber(BENCE_IBAN).build();
        Beneficiary landlord = Beneficiary.builder().id(2L).accountNumber(LANDLORD_IBAN).build();
        when(beneficiaryRepository.findByUser_IdOrderByNameAsc(7L)).thenReturn(List.of(bence, landlord));
        when(accountRepository.existsByAccountNumber(BENCE_IBAN)).thenReturn(true);
        when(accountRepository.existsByAccountNumber(LANDLORD_IBAN)).thenReturn(false);
        when(beneficiaryMapper.toResponse(bence, true)).thenReturn(BeneficiaryResponse.builder().id(1L).internal(true).build());
        when(beneficiaryMapper.toResponse(landlord, false)).thenReturn(BeneficiaryResponse.builder().id(2L).internal(false).build());

        List<BeneficiaryResponse> out = service.listMine();

        assertEquals(2, out.size());
        assertTrue(out.get(0).isInternal());
        assertFalse(out.get(1).isInternal());
    }

    @Test
    void create_shouldSaveNormalizedAccountNumber() {
        User user = User.builder().id(7L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(beneficiaryRepository.existsByUser_IdAndAccountNumber(7L, BENCE_IBAN)).thenReturn(false);

        CreateBeneficiaryRequest req = CreateBeneficiaryRequest.builder()
                .name("Nagy Bence")
                .accountNumber("hu28 1040 0095 0000 5217 0000 0003")
                .build();

        Beneficiary mapped = Beneficiary.builder().name("Nagy Bence").accountNumber(BENCE_IBAN).build();
        when(beneficiaryMapper.toEntity(req, user, BENCE_IBAN)).thenReturn(mapped);

        Beneficiary saved = Beneficiary.builder().id(5L).name("Nagy Bence").accountNumber(BENCE_IBAN).build();
        when(beneficiaryRepository.save(mapped)).thenReturn(saved);
        when(accountRepository.existsByAccountNumber(BENCE_IBAN)).thenReturn(true);
        when(beneficiaryMapper.toResponse(saved, true)).thenReturn(BeneficiaryResponse.builder().id(5L).accountNumber(BENCE_IBAN).internal(true).build());

        BeneficiaryResponse resp = service.create(req);

        assertEquals(5L, resp.getId());
        assertEquals(BENCE_IBAN, resp.getAccountNumber());
        assertTrue(resp.isInternal());
    }

    @Test
    void create_shouldThrowBusinessException_whenIbanIsInvalid() {
        when(currentUser.requireEntity()).thenReturn(User.builder().id(7L).build());

        CreateBeneficiaryRequest req = CreateBeneficiaryRequest.builder()
                .name("Hibás")
                .accountNumber("HU28104000950000521700000004")
                .build();

        assertThrows(BusinessException.class, () -> service.create(req));

        verifyNoInteractions(beneficiaryRepository, beneficiaryMapper);
    }

    @Test
    void create_shouldThrowBusinessException_whenAlreadySaved() {
        when(currentUser.requireEntity()).thenReturn(User.builder().id(7L).build());
        when(beneficiaryRepository.existsByUser_IdAndAccountNumber(7L, BENCE_IBAN)).thenReturn(true);

        CreateBeneficiaryRequest req = CreateBeneficiaryRequest.builder()
                .name("Bence")
                .accountNumber(BENCE_IBAN)
                .build();

        assertThrows(BusinessException.class, () -> service.create(req));

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateOwnBeneficiary() {
        when(currentUser.requireId()).thenReturn(7L);

        Beneficiary beneficiary = Beneficiary.builder().id(5L).user(User.builder().id(7L).build()).name("Régi").accountNumber(BENCE_IBAN).build();
        when(beneficiaryRepository.findByIdAndUser_Id(5L, 7L)).thenReturn(Optional.of(beneficiary));
        when(beneficiaryRepository.existsByUser_IdAndAccountNumberAndIdNot(7L, LANDLORD_IBAN, 5L)).thenReturn(false);
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(beneficiaryMapper.toResponse(beneficiary, false)).thenReturn(BeneficiaryResponse.builder().id(5L).build());

        UpdateBeneficiaryRequest req = UpdateBeneficiaryRequest.builder()
                .name("Tóth Gábor")
                .accountNumber(LANDLORD_IBAN)
                .note("Albérlet")
                .build();

        service.update(5L, req);

        verify(beneficiaryMapper).updateEntity(beneficiary, req, LANDLORD_IBAN);
    }

    @Test
    void update_shouldThrowBusinessException_whenAccountNumberUsedByOtherBeneficiary() {
        when(currentUser.requireId()).thenReturn(7L);

        Beneficiary beneficiary = Beneficiary.builder().id(5L).user(User.builder().id(7L).build()).accountNumber(BENCE_IBAN).build();
        when(beneficiaryRepository.findByIdAndUser_Id(5L, 7L)).thenReturn(Optional.of(beneficiary));
        when(beneficiaryRepository.existsByUser_IdAndAccountNumberAndIdNot(7L, LANDLORD_IBAN, 5L)).thenReturn(true);

        UpdateBeneficiaryRequest req = UpdateBeneficiaryRequest.builder()
                .name("Tóth Gábor")
                .accountNumber(LANDLORD_IBAN)
                .build();

        assertThrows(BusinessException.class, () -> service.update(5L, req));

        verify(beneficiaryMapper, never()).updateEntity(any(), any(), any());
    }

    @Test
    void delete_shouldDeleteOwnBeneficiary() {
        when(currentUser.requireId()).thenReturn(7L);

        Beneficiary beneficiary = Beneficiary.builder().id(5L).build();
        when(beneficiaryRepository.findByIdAndUser_Id(5L, 7L)).thenReturn(Optional.of(beneficiary));

        service.delete(5L);

        verify(beneficiaryRepository).delete(beneficiary);
    }

    @Test
    void delete_shouldThrowNotFound_whenNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(beneficiaryRepository.findByIdAndUser_Id(5L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(5L));

        verify(beneficiaryRepository, never()).delete(any());
    }
}
