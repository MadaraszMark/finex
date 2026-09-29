package hu.finex.main.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.StandingOrder;

@Repository
public interface StandingOrderRepository extends JpaRepository<StandingOrder, Long> {

    // A felhasználó megbízásai (a forrásszámlákon keresztül)
    List<StandingOrder> findByAccount_User_IdOrderByCreatedAtDesc(Long userId);

    // Csak akkor adja vissza, ha a forrásszámla a felhasználóé (jogosultság-ellenőrzés)
    Optional<StandingOrder> findByIdAndAccount_User_Id(Long id, Long userId);

    // Az ütemező által teljesítendő, esedékes aktív megbízások
    @Query("select o.id from StandingOrder o where o.active = true and o.nextExecutionDate <= :date order by o.nextExecutionDate")
    List<Long> findDueOrderIds(@Param("date") LocalDate date);

    // Egy számla aktív megbízásai (a számla lezárásakor le kell állítani őket)
    List<StandingOrder> findByAccount_IdAndActiveTrue(Long accountId);

    // A megbízás tulajdonosa, a forrásszámla betöltése nélkül (az utalás előtt a számlát zárolni kell)
    @Query("select o.account.user.id from StandingOrder o where o.id = :id")
    Optional<Long> findOwnerIdById(@Param("id") Long id);
}
