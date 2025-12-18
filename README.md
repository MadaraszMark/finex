## Finex – Digitális banki REST Backend (folyamatban)

### Technológiák:
- Java 17+
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA (Hibernate)
- Lombok
- REST API-k
- Jakarta Validation
- Spring Security (JWT)
- Swagger (Springdoc OpenAPI)
- PostgreSQL
- Unit tesztek (JUnit 5 & Mockito, később)

---

## Mi ez?
Saját gyakorló projekt, ahol egy digitális banki backend rendszert építek fel modulárisan Spring Boot segítségével.  
A projekt célja, hogy **valós banki funkciókat** modellezzek és gyakoroljak (felhasználók, bankszámlák, tranzakciók, megtakarítások, átutalások, ügyfélszolgálat).

A backend frontend nélkül, Postman segítségével tesztelhető.  
A későbbiekben egy **SwiftUI alapú iOS kliens alkalmazás** készül hozzá.

---

## Jelenleg kész:
- **Alap konfigurációk**
  - Spring Boot alkalmazás indítása
  - PostgreSQL adatbázis kapcsolat
  - Global Exception Handling
  - Auditing (createdAt, updatedAt)
- **Biztonság**
  - JWT alapú autentikáció
  - Stateless SecurityConfig
  - Védett endpointok (csak bejelentkezett felhasználók)
- **Felhasználók**
  - Regisztráció és bejelentkezés
- **Bankszámlák**
  - Folyószámlák kezelése
  - Egyenleg és devizanem
- **Tranzakciók**
  - Bevételek és kiadások
  - Átutalások számlák között
  - Egyenleg history
- **Megtakarítások**
  - Megtakarítási számlák
  - Pénz áthelyezése folyószámláról / folyószámlára
- **Ügyfélszolgálat**
  - Support ticket rendszer
  - Ticket nyitás csak bejelentkezett felhasználóknak

---

## Tervezett funkciók:
- **Jogosultságkezelés**
  - Admin / User role-ok
  - Admin-only endpointok
- **Tranzakciók bővítése**
  - Tranzakciók szűrése (időszak, összeg, típus)
  - Kategóriák és statisztikák
- **Megtakarítások**
  - Automatikus havi megtakarítás
  - Kamat számítás
- **Értesítések**
  - Tranzakciós értesítések
  - Support ticket válasz értesítések
- **Audit és naplózás**
  - Kritikus műveletek logolása
- **SwiftUI frontend**
  - iOS banki alkalmazás
  - Bejelentkezés / regisztráció
  - Számlák és tranzakciók megjelenítése
  - Megtakarítások kezelése
  - Support ticket felület
- **UI / UX tervezés**
  - Figma alapú design
  - Modern mobilbanki kinézet
