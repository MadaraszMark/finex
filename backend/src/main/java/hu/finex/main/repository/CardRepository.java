package hu.finex.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.Card;
import hu.finex.main.model.enums.CardStatus;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // A felhasználó összes kártyája (a számlákon keresztül)
    List<Card> findByAccount_User_IdOrderByCreatedAtAsc(Long userId);

    // Csak akkor adja vissza a kártyát, ha a felhasználó számlájához tartozik (jogosultság-ellenőrzés)
    Optional<Card> findByIdAndAccount_User_Id(Long id, Long userId);

    // A kártyához tartozó számla azonosítója, az entitások betöltése nélkül (a számla zárolása előtt)
    @Query("select c.account.id from Card c where c.id = :id and c.account.user.id = :userId")
    Optional<Long> findAccountIdByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // Egy számla kártyái (pl. lezáráskor mindet meg kell szüntetni)
    List<Card> findByAccount_Id(Long accountId);

    // Generált kártyaszám ütközésének ellenőrzése
    boolean existsByCardNumber(String cardNumber);

    long countByAccount_User_IdAndStatus(Long userId, CardStatus status);
}
