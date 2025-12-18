## Finex – Digitális banki mobile applikáció (folyamatban)

### Technológiák:
- Java 17+
- Spring Boot 3.5.4
- Spring Web
- Spring Data JPA (Hibernate)
- Lombok
- REST API-k
- Jakarta Validation
- Spring Security (JWT)
- Swagger (Springdoc OpenAPI)
- PostgreSQL
- Unit tesztek

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
 
---

## Tervezett mobilalkalmazás (SwiftUI – WIP)

A Finex backendhez egy **natív iOS mobilbanki alkalmazás** készül SwiftUI technológiával.  
Az alkalmazás célja, hogy a backend funkcióit egy **modern, letisztult mobilbanki felületen** tegye elérhetővé.

⚠️ **Megjegyzés:**  
Az alábbi UI képek és leírások **koncepciótervek**, a végleges funkcionalitás és megjelenés a fejlesztés során változhat.

![Finex iOS App – Planned UI](README-assets/FineX-Home.jpg)

---

## Tervezett képernyők (SwiftUI)

- **Bejelentkezés / Regisztráció**
  - JWT alapú autentikáció
- **Főoldal / Dashboard**
  - Egyenlegek áttekintése
  - Gyors műveletek
- **Bankszámlák**
  - Folyószámlák listája
  - Egyenleg és devizanem
- **Tranzakciók**
  - Tranzakció lista
  - Szűrés és részletek
- **Megtakarítások**
  - Megtakarítási számlák kezelése
  - Pénz áthelyezés
- **Ügyfélszolgálat**
  - Support ticket létrehozása
  - Ticketek állapotának követése  

