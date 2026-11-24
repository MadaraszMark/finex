# FineX – REST API végpontjai

A backend 83 végpontja 24 controllerben, a controllerek `@Operation` leírásai alapján (40 GET, 22 POST, 7 PUT, 8 PATCH, 6 DELETE). A kérések és válaszok mezői, a validációs szabályok és a lehetséges hibakódok a Swagger UI-ban láthatók: http://localhost:8080/swagger-ui.html (Docker Compose-zal: http://localhost:3000/api/swagger-ui.html).

## Általános szabályok

- **Hitelesítés:** a `/auth/**` végpontok kivételével minden kéréshez `Authorization: Bearer <JWT>` fejléc kell. A tokent a `POST /auth/login` adja, 1 óráig érvényes.
- **Jogosultság:** a `/admin/**` végpontokat csak ADMIN szerepkörű felhasználó hívhatja. A többi végpont csak a bejelentkezett felhasználó saját adataihoz fér hozzá; más felhasználó adatára 404 a válasz.
- **Idempotencia:** az utalás, a devizaváltás és a kártyás fizetés opcionális `Idempotency-Key` fejlécet fogad (8–100 karakter: betű, szám, kötőjel, aláhúzás). Azonos kulccsal és azonos kéréssel a korábbi válasz jön vissza, újabb könyvelés nélkül.
- **Hibaválasz** (`ApiError`): `timestamp`, `status`, `message`, validációs hibánál `violations` (mezőnként). Fő státuszkódok: 400 üzleti vagy validációs hiba, 401 hiányzó vagy érvénytelen token, 403 nincs jogosultság, 404 nem található, 429 túl sok kérés (`Retry-After` fejléccel).
- **Lapozás:** a listázó végpontok `page`, `size` (legfeljebb 100) és `sort` paramétert fogadnak.

## Hitelesítés

Hozzáférés: nyilvános (belépés nélkül).

| Metódus | Útvonal | Leírás |
|---|---|---|
| `POST` | `/auth/login` | Bejelentkezés |
| `POST` | `/auth/register` | Regisztráció |

## Saját felhasználói fiók

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/users/me` | Bejelentkezett felhasználó adatainak lekérése |
| `PUT` | `/users/me` | Saját adatok módosítása |
| `PUT` | `/users/me/password` | Jelszócsere |
| `POST` | `/users/me/logout` | Kijelentkezés |
| `POST` | `/users/me/logout-all` | Kijelentkezés minden eszközről |

## Bankszámlák

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/accounts` | Saját bankszámlák listája |
| `POST` | `/accounts` | Új bankszámla nyitása (bankkártyával együtt) |
| `GET` | `/accounts/{id}` | Saját bankszámla lekérdezése ID alapján |
| `GET` | `/accounts/me` | Elsődleges folyószámla (a legrégebbi aktív folyószámla) |
| `GET` | `/accounts/{id}/statement.csv` | Számlakivonat letöltése CSV-ben (Excelhez) |
| `GET` | `/accounts/{id}/statement` | Számlakivonat egy időszakra |
| `DELETE` | `/accounts/{id}` | Bankszámla lezárása (CLOSED státusz) |

## Egyenlegtörténet

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/balance-history/account/{accountId}` | Egy saját számla egyenlegének alakulása (grafikonhoz) |

## Bankkártyák

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/cards` | Saját bankkártyák (maszkolt kártyaszámmal) |
| `GET` | `/cards/{id}` | Saját bankkártya lekérdezése ID alapján |
| `PATCH` | `/cards/{id}/block` | Kártya ideiglenes letiltása (pl. elvesztés esetén) |
| `PATCH` | `/cards/{id}/unblock` | Kártya feloldása |
| `PUT` | `/cards/{id}/limits` | Napi limit és online fizetés beállítása |
| `POST` | `/cards/{id}/payments` | Kártyás fizetés szimulálása (demó) |

## Tranzakciók, utalás, devizaváltás

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/transactions` | Saját tranzakciók keresése (lapozható) |
| `GET` | `/transactions/{id}` | Saját tranzakció lekérdezése ID alapján |
| `POST` | `/transactions/transfer` | Utalás IBAN számlaszámra |
| `POST` | `/transactions/exchange` | Devizaváltás két saját számla között |

## Tranzakciók kategorizálása

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `POST` | `/transaction-categories/{transactionId}/assign/{categoryId}` | Kategória hozzárendelése egy saját tranzakcióhoz |
| `GET` | `/transaction-categories/transaction/{transactionId}` | Egy saját tranzakcióhoz tartozó kategóriák listázása |
| `DELETE` | `/transaction-categories/{id}` | Tranzakció–kategória kapcsolat törlése |

## Kategóriák

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/categories/{id}` | Kategória lekérdezése ID alapján |
| `GET` | `/categories` | Összes kategória listázása név szerint |

## Kedvezményezettek

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/beneficiaries` | Saját kedvezményezettek név szerint |
| `POST` | `/beneficiaries` | Új kedvezményezett mentése |
| `PUT` | `/beneficiaries/{id}` | Kedvezményezett módosítása |
| `DELETE` | `/beneficiaries/{id}` | Kedvezményezett törlése |

## Rendszeres átutalások

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/standing-orders` | Saját rendszeres átutalások |
| `POST` | `/standing-orders` | Új rendszeres átutalás |
| `PUT` | `/standing-orders/{id}` | Rendszeres átutalás módosítása (összeg, közlemény, gyakoriság, következő esedékesség) |
| `DELETE` | `/standing-orders/{id}` | Rendszeres átutalás megszüntetése |

## Megtakarítások

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/savings` | Saját (nem lezárt) megtakarítások |
| `POST` | `/savings` | Új megtakarítás létrehozása |
| `GET` | `/savings/{id}` | Saját megtakarítás lekérdezése ID alapján |
| `PUT` | `/savings/{id}` | Megtakarítás átnevezése és célösszeg módosítása |
| `POST` | `/savings/{id}/deposit-from-account` | Pénz átvezetése folyószámláról megtakarítási számlára |
| `POST` | `/savings/{id}/withdraw-to-account` | Pénz kivétele megtakarítási számláról folyószámlára |
| `GET` | `/savings/{id}/transactions` | A megtakarítás mozgásai (befizetés, kivét, kamat), lapozva |
| `DELETE` | `/savings/{id}` | Megtakarítás lezárása |

## Statisztika

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/statistics/summary` | A főoldal összesítője (egyenlegek, e havi bevétel/kiadás, olvasatlan értesítések) |
| `GET` | `/statistics/monthly` | Havi bevétel és kiadás az elmúlt N hónapra (oszlopdiagramhoz) |
| `GET` | `/statistics/categories` | Költések kategóriánként (kördiagramhoz) |
| `GET` | `/statistics/top-expenses` | Az időszak legnagyobb kiadásai egy devizában |

## Értesítések

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/notifications` | Saját értesítések (lapozva, a legújabb elöl) |
| `GET` | `/notifications/unread-count` | Olvasatlan értesítések száma (a csengő ikon jelvényéhez) |
| `PATCH` | `/notifications/{id}/read` | Értesítés olvasottra állítása |
| `PATCH` | `/notifications/read-all` | Összes értesítés olvasottra állítása |

## Ügyfélszolgálat

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `POST` | `/support-tickets` | Új support ticket létrehozása |
| `GET` | `/support-tickets` | Saját ticketek (lapozva, a legújabb elöl) |
| `GET` | `/support-tickets/{id}` | Saját ticket a teljes beszélgetéssel |
| `POST` | `/support-tickets/{id}/messages` | Válasz a saját ticketre |

## Belépési előzmények

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/login-logs/me` | Saját bejelentkezési előzmények (eszköz, IP, sikeres / sikertelen) |

## Üzleti szabályok

Hozzáférés: bejelentkezett felhasználó.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/settings` | Az üzleti szabályok paraméterei a frontend számára |

## Admin: áttekintés és ütemezett feladatok

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/admin/dashboard` | A bank egészére vonatkozó mutatók |
| `POST` | `/admin/jobs/savings-interest` | Havi kamatjóváírás azonnali futtatása (megtakarításonként havonta csak egyszer ír jóvá) |
| `POST` | `/admin/jobs/standing-orders` | Az esedékes rendszeres átutalások azonnali teljesítése |

## Admin: felhasználók

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/admin/users` | Felhasználók keresése névre vagy e-mail címre (lapozva) |
| `GET` | `/admin/users/{id}` | Egy felhasználó teljes áttekintése (számlák, kártyák, megtakarítások) |
| `PATCH` | `/admin/users/{id}/status` | Felhasználó letiltása vagy feloldása |
| `PATCH` | `/admin/users/{id}/role` | Felhasználó szerepkörének módosítása |

## Admin: számlák

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/admin/accounts` | Összes számla, opcionálisan státusz szerint (lapozva) |
| `PATCH` | `/admin/accounts/{id}/status` | Számla státuszának módosítása (befagyasztás, tiltás, feloldás, lezárás) |
| `GET` | `/admin/accounts/{id}/transactions` | Egy számla tételei (lapozva, legfrissebb elöl) |
| `POST` | `/admin/accounts/{id}/deposit` | Készpénzbefizetés rögzítése egy számlára |

## Admin: sztornó és egyeztetés

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `POST` | `/admin/transactions/{id}/reverse` | Tétel sztornózása |
| `GET` | `/admin/reconciliation` | Főkönyvi egyeztetés |

## Admin: ticketek

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/admin/support-tickets` | Összes ticket, opcionálisan státusz szerint (lapozva) |
| `GET` | `/admin/support-tickets/{id}` | Ticket a teljes beszélgetéssel |
| `POST` | `/admin/support-tickets/{id}/messages` | Válasz a ticketre (a nyitott ticket folyamatban állapotba kerül, a felhasználó értesítést kap) |
| `PATCH` | `/admin/support-tickets/{id}/status` | Ticket státuszának módosítása |

## Admin: belépési napló

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/admin/login-logs` | Bejelentkezési események keresése |
| `GET` | `/admin/login-logs/{id}` | Login log lekérdezése ID alapján |

## Admin: audit napló

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `GET` | `/admin/audit-log` | Adminműveletek keresése |

## Admin: kategóriák

Hozzáférés: csak ADMIN szerepkör.

| Metódus | Útvonal | Leírás |
|---|---|---|
| `POST` | `/admin/categories` | Új kategória létrehozása |
| `PUT` | `/admin/categories/{id}` | Kategória módosítása |
| `DELETE` | `/admin/categories/{id}` | Kategória törlése (csak ha egy tranzakció sem használja) |
