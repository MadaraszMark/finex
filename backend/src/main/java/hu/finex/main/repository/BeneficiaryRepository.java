package hu.finex.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.Beneficiary;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    // A felhasználó mentett kedvezményezettjei név szerint
    List<Beneficiary> findByUser_IdOrderByNameAsc(Long userId);

    // Csak akkor adja vissza, ha a megadott felhasználóé (jogosultság-ellenőrzés)
    Optional<Beneficiary> findByIdAndUser_Id(Long id, Long userId);

    // Egy számlaszám csak egyszer szerepelhet a listában
    boolean existsByUser_IdAndAccountNumber(Long userId, String accountNumber);

    boolean existsByUser_IdAndAccountNumberAndIdNot(Long userId, String accountNumber, Long id);
}
