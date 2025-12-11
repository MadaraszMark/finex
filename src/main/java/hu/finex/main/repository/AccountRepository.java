package hu.finex.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.Account;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    //Számlaszám alapján lekérés (egyedi)
    Optional<Account> findByAccountNumber(String accountNumber);
    
    Optional<Account> findFirstByUser_IdAndAccountType(Long userId, AccountType accountType);

    //User összes számlája
    List<Account> findByUser_Id(Long userId);

    //Csak aktív / élő számlák szűrése
    List<Account> findByUser_IdAndStatus(Long userId, AccountStatus status);

    //Státusz ellenőrzés
    boolean existsByAccountNumberAndStatus(String accountNumber, AccountStatus status);

    //Account-number prefix keresés (pl. HUF számlák)
    List<Account> findByAccountNumberStartingWith(String prefix);
}

