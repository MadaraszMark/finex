# FineX – fejlesztői dokumentáció

Architektúra, konvenciók, ellenőrzés, ismert buktatók és a git-munkafolyamat. A funkciók listája a gyökérben lévő
[`README.md`](../README.md)-ben, a telepítés és indítás a [`TELEPITES.md`](TELEPITES.md)-ben, az adatbázis leírása a
[`database/README.md`](../database/README.md)-ben van.

---

## 1. A projekt

- **Mi ez:** a FineX egy demó online bank, Madarász Márk **BSc szakdolgozata** (webfejlesztés: Java Spring Boot +
  React). A cél a backend- és üzleti logika, valamint az adatbázis-ismeret bemutatása (ezért van Flyway, trigger,
  SQL-függvény, nézet, részleges index).
- **Repó:** https://github.com/MadaraszMark/finex (publikus). Monorepo:

| Mappa | Tartalom |
|---|---|
| `backend/` | Spring Boot 3.5.16 REST API, Java 17 target (JDK 21-en fut), Maven wrapper (`mvnw.cmd`, Maven 3.9.11) |
| `frontend/` | React 19 + TypeScript + Vite 8 + Tailwind CSS 4 |
| `database/` | `docker-compose.yml` (PostgreSQL 15), a séma és a demóadatok leírása |
| gyökér | `docker-compose.yml`: a teljes alkalmazás egy paranccsal (adatbázis + backend + nginx-es frontend), `.env.example` |
| `docs/` | Ez a dokumentáció, telepítési útmutató, UI koncepcióképek (`images/` – iPhone/SwiftUI mockupok) |

Fejlesztőeszközök: backend Eclipse (Spring Tools), frontend VS Code, Windows 11.

---

## 2. Állapot és előzmények

**Kész:**

- **Backend:** felhasználók, regisztráció (automatikus folyószámla + kártya), JWT, USER/ADMIN szerepkör, számlák
  (HUF/EUR/USD, státuszok, kivonat futó egyenleggel, CSV-export), kártyák (tiltás, limitek, online fizetés, demó
  kártyás fizetés), utalás (napi limit, fedezet, párhuzamos utalásoknál is konzisztens), devizaváltás saját számlák
  között, kategóriák, kedvezményezettek, rendszeres átutalások + ütemező, megtakarítások + kamatütemező, értesítések,
  statisztikák, ügyfélszolgálati ticketek, belépési napló + zárolás, admin API (befizetés, sztornó, egyeztetés,
  audit napló), `GET /settings`. **512 automata teszt.**
- **Adatbázis:** Flyway V1–V9 + demóadat (V4_1), `ddl-auto=validate`.
- **Frontend:** mind az öt rész (alapok; főoldal, számlák, kártyák; pénzmozgás; tervezés; fiók és adminisztráció),
  aurora/üveg design, szimmetrikus kártyarácsok, skeleton-betöltők, Ctrl+K parancspaletta, natív `<dialog>` ablakok
  (mobilon lehúzható alsó lap), körben szétterülő témaváltás, az olvasatlan értesítések száma a böngészőfül címében. **21 Vitest teszt.**
- **Futtatás:** Docker Compose-zal egy paranccsal is (`docker compose up -d --build --wait`).

**Előzmények:**

| Dátum | Mi készült |
|---|---|
| 2025-12 – 2026-02 | Az eredeti backend (entitások, DTO-k, JWT, service-ek, controllerek, mapper-, service- és controllertesztek) |
| 2026-09-29 | Monorepo, Flyway-séma és teljes banki funkciókészlet, frontend alapok (`5083355`, `ef27afc`, `a61a08e`) |
| 2026-10-20 | Frontend 2–5. rész és újratervezés, `GET /settings` (`5fcb21c`, `b13a379`) |
| 2026-10-26 | Biztonsági megerősítés, idempotencia, sztornó, egyeztetés, audit napló, devizaváltás, CSV-kivonat, Vitest, Docker Compose |
| 2026-10-27 | Kommentek egységesítése; a régi, Flyway előtti pgAdmin-mentés kikerült a repóból |

### Teendők, ötletek

1. GitHub „About” leírás frissítése, például: „FineX – digitális bank: Spring Boot REST API + React webes felület
   (BSc szakdolgozat)”.
2. Tervezett funkciók (README): automatikus havi megtakarítás; SwiftUI mobilalkalmazás (egyelőre csak koncepció).
3. Továbblépési lehetőségek: PDF-kivonat (most CSV van), valós árfolyam-forrás (most rögzített demóárfolyam a
   `finex.exchange-rates.*`-ban), a kérésszám-korlát közös tárolóba (most memóriában, egy példányra jó).

---

## 3. Backend

Csomag: `hu.finex.main` (`backend/src/main/java/hu/finex/main/`)

| Csomag | Szerep |
|---|---|
| `controller/` | REST végpontok, Swagger-annotációkkal. Admin végpontok: `Admin*Controller`, útvonal `/admin/**` (`hasRole("ADMIN")`) |
| `service/` | Üzleti logika. `LedgerService`: **minden egyenlegváltozás** ezen megy át → tranzakció + `balance_history` sor |
| `repository/` | Spring Data JPA; zárolás: `@Lock(PESSIMISTIC_WRITE)` (számla, megtakarítás), mindig **id szerinti sorrendben** (holtpont ellen) |
| `model/`, `model/enums/` | JPA entitások (Lombok builder) |
| `dto/` | Request/Response osztályok, Jakarta Validation + `@Schema` |
| `mapper/` | Kézzel írt `@Component` mapperek (nincs MapStruct) |
| `security/` | `JwtAuthenticationFilter` (aláírás, lejárat, `jti` tiltólista, tokenverzió), `JwtTokenUtil` (jjwt 0.12), `RateLimitFilter` (sima osztály, a `SecurityConfig` példányosítja – `@Component`-ként a `@WebMvcTest`-ekbe is bekerülne), `CurrentUser` (a principal = felhasználó id; `requireId()`, `requireEntity()`, `ensureSameUser()`) |
| `config/` | `SecurityConfig` (stateless, CORS, fejlécek, szűrők), `FinexProperties` (`finex.*` üzleti szabályok), `FinexConfig` (Jackson korlát), `ProductionSafetyCheck` (`@Profile("prod")`), `JpaAuditingConfig` (`@Profile("!test")`), `OpenApiConfig` |
| `scheduler/` | `StandingOrderScheduler` (óránként, :05), `SavingsInterestScheduler` (óránként, :15; havonta egyszer ír jóvá) – az admin kézzel is indíthatja; `CleanupScheduler` (óránként, :25: lejárt visszavont tokenek, 7 napnál régebbi idempotencia-kulcsok) |
| `exception/` | `BusinessException` (400), `NotFoundException` (404), `GlobalExceptionHandler` → `ApiError` (minden ismeretlen hiba 500 + általános üzenet, a részlet csak a naplóba) |
| `util/` | `DateUtils` (`requireReportRange` ≤ 366 nap), `IbanUtils`, `MoneyUtils`, `PasswordPolicy` (72 bájt), `CsvUtils` (CSV-injekció elleni védelem) |

**Biztonsági és könyvelési építőelemek:**

| Elem | Hol | Lényeg |
|---|---|---|
| Visszavonható token | `User.tokenVersion`, `RevokedTokenService`, `UserService.logoutEverywhere` | A tokenben `ver` és `jti`; a szűrő elutasítja, ha a verzió nem egyezik vagy a `jti` tiltólistán van |
| Zárolás | `LoginLogService` | e-mail + IP 5, fiók 20, IP 20 hiba / 15 perc; a zárolás miatti elutasítás nem számít újabb hibának |
| Kérésszám-korlát | `RateLimitFilter` | Belépés percenként, regisztráció óránként IP-címenként (429 + `Retry-After`), 64 KB-os kérésméret |
| Idempotencia | `IdempotencyService.execute(key, operation, request, responseType, supplier)` | `INSERT … ON CONFLICT DO NOTHING` ugyanabban a tranzakcióban; azonos kulcs + azonos kérés → a tárolt válasz, más kérés → 400 |
| Audit | `AuditService.record(...)` (`Propagation.MANDATORY`) | Az admin művelettel egy tranzakcióban ír; a tábla triggerrel módosíthatatlan |
| Sztornó | `TransactionService.reverse` → `LedgerService.reverse` | Ellentétes tétel `reversal_of_id`-val (egyedi); engedett: kártyás fizetés, admin befizetés, kimenő utalás (a bejövő láb is) |
| Devizaváltás | `TransactionService.exchange`, `ExchangeRateService` | Rögzített árfolyam (`finex.exchange-rates.*`), lefelé kerekítve; a napi limit forintértékben számít |
| Egyeztetés | `ReconciliationService`, `ReportRepository` | `v_account_reconciliation` / `v_savings_reconciliation` nézet: tárolt egyenleg = tételek összege (`difference`), megtakarításnál = utolsó `balance_after` is |

- **Jogosultság:** minden felhasználó csak a saját adatait látja: a service-ekben `findOwned(id)` minta
  (`repository.findByIdAndUser_Id(id, currentUser.requireId())` → `NotFoundException`).
- **Konfiguráció** (`application.properties`): környezeti változók alapértékkel – `FINEX_DB_URL`
  (`jdbc:postgresql://localhost:5432/finex`), `FINEX_DB_USERNAME` (`finex`), `FINEX_DB_PASSWORD` (`finex123`),
  `FINEX_JWT_SECRET` (nincs alapérték), `FINEX_SHOW_SQL` (`false`), `FINEX_SWAGGER_ENABLED` (`true`),
  `FINEX_FORWARD_HEADERS_STRATEGY` (`none`; a Docker Compose-ban `framework`, mert ott csak az nginx éri el a backendet).
  JWT lejárat 1 óra. `finex.cors-allowed-origins=http://localhost:5173`.
  Üzleti szabályok: `finex.transfer-daily-limit`, `finex.card-default-daily-limit(-foreign)`, `finex.card-max-daily-limit`,
  `finex.savings-interest-rate`, `finex.max-accounts-per-user`, `finex.max-savings-per-user`,
  `finex.max-standing-orders-per-user`, `finex.max-beneficiaries-per-user`, `finex.login-*`, `finex.rate-limit-*`,
  `finex.exchange-rates.*`. A `prod` profil (`application-prod.properties`) kikapcsolja a demóadatot és a Swaggert.
- **`GET /settings`** (`BankSettingsController` → `BankSettingsService` → `BankSettingsResponse`): a frontend innen
  olvassa a napi utalási limitet, a max. számlaszámot, a kamatlábat, a kártyalimiteket, a darabszám-korlátokat és az árfolyamokat.
- **Fontosabb végpontok a 2026-10-26-i bővítésből:** `PUT /users/me/password` (új tokent ad vissza),
  `POST /users/me/logout`, `POST /users/me/logout-all`, `POST /transactions/exchange`, `GET /accounts/{id}/statement.csv`,
  `GET /statistics/top-expenses`, `GET /admin/accounts/{id}/transactions`, `POST /admin/accounts/{id}/deposit`,
  `POST /admin/transactions/{id}/reverse`, `GET /admin/reconciliation`, `GET /admin/audit-log`.
  Megszűnt: `POST /accounts/{id}/deposit` (az ügyfél ne írhasson jóvá magának pénzt).

### Backend kódstílus

Új kód a meglévő mintákat követi, például: `controller/BeneficiaryController.java`, `service/BeneficiaryService.java`,
`mapper/BeneficiaryMapper.java`, `dto/BeneficiaryResponse.java`.

- 4 szóközös behúzás; Lombok `@RequiredArgsConstructor` + `private final` mezők.
- Controller: `ResponseEntity<…>`, `@Tag(name = "… API", description = "…")`, és minden végponton
  `@Operation(summary = "…",responses = { @ApiResponse(responseCode = "…", description = "…") })` – pontosan ebben a
  formában (vessző után szóköz nélkül a `responses` előtt, a záró `}` és `)` külön sorban).
  `@PathVariable("id") Long id` – explicit névvel. Létrehozás: `ResponseEntity.status(HttpStatus.CREATED).body(…)`,
  törlés: `ResponseEntity.noContent().build()`.
- DTO: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` + `@Schema(description = "…")` az osztályon,
  minden mezőn `@Schema(description, example)`, `import lombok.*;`.
- Service: olvasásnál `@Transactional(readOnly = true)`, írásnál `@Transactional`; magyar hibaüzenetek
  `BusinessException` / `NotFoundException`-nel; privát segédmetódusok a végén.
- Mapper: `toEntity`, `updateEntity`, `toResponse` builderrel.
- Repository: származtatott lekérdezésnevek (`findByUser_IdOrderByNameAsc`).
- Kommentek: magyarul, ritkán, mindig egy sor (kb. 40–100 karakter) – a „miért”-et vagy az üzleti szabályt mondják,
  ne a kódot ismételjék. Nincs többsoros blokk, Javadoc, elválasztó vonal. Például:
  `// Bejelentkezés és regisztráció üzleti logikája (JWT + login naplózás)`.
- Tesztek: `src/test/java/hu/finex/main/` – mapper unit, Mockito service, `@WebMvcTest` controller,
  `@DataJpaTest` Testcontainers PostgreSQL-lel (`repository/PostgresRepositoryTestBase.java`), párhuzamossági
  integrációs teszt (`service/TransferConcurrencyIntegrationTest.java`).

### Adatbázis

- PostgreSQL 15 Dockerben (`database/docker-compose.yml`, projektnév `finex`, konténer `finex-postgres`, volume
  `finex_finex-pgdata`). Részletek: [`database/README.md`](../database/README.md).
- **Flyway**: `backend/src/main/resources/db/migration/V1…V9` + `db/demo/V4_1__demo_data.sql`. Lefuttatott
  migrációt **tilos módosítani** (a checksum a kommentekre is érzékeny) – sémaváltozáshoz új fájl kell: `V10__….sql`.
  V5 biztonság (tokenverzió, `revoked_tokens`, kártyabirtokos neve, havi egyszeri kamat egyedi index,
  `savings_min_balance()`, `standing_orders.execution_day`), V6 `audit_log` (módosíthatatlan), V7 tételkapcsolatok és
  sztornó (`related_transaction_id`, `reversal_of_id`, pénznem-ellenőrző trigger, módosíthatatlan `balance_history`,
  `is_cashflow()`), V8 `idempotency_keys`, V9 egyeztető nézetek.
- Bemutató elemek: `CHECK` megszorítások, részleges unique index, kifejezés-alapú egyedi index, a könyvelt tételeket
  védő triggerek, `account_balance_at` / `account_statement` / `savings_min_balance` / `is_cashflow` függvények,
  `v_account_monthly_summary`, `v_account_reconciliation`, `v_savings_reconciliation` nézetek.
  Az `is_cashflow()` JPQL-ből is hívható: `function('is_cashflow', t.id) = true`.
- A tesztek (Testcontainers, `postgres:15-alpine`) csak a `migration` mappát futtatják, a demóadatot nem.

---

## 4. Frontend

`frontend/src/`:

| Mappa | Tartalom |
|---|---|
| `app/` | `router.tsx` (lazy route-ok, `handle.title`, minden route-nak `hydrateFallbackElement` skeleton), `layout/` (Sidebar, TopBar, BottomNav, MenuSheet, UserMenu), `CommandPalette.tsx` (Ctrl+K), `navigation.ts` (menüszerkezet) |
| `api/` | **1:1 a backend controllerekkel** (`accounts.ts`, `admin.ts`, …), `client.ts` (axios, JWT, 401 → kiléptetés), `types.ts` (a DTO-k tükre), `queryKeys.ts` (hierarchikus React Query kulcsok) |
| `features/<funkció>/` | `pages/`, `components/` (benne az oldal `…Skeleton`-je), `queries.ts` (React Query hookok), `schemas.ts` (zod) |
| `components/ui/` | Saját Tailwind komponensek (Button, Card, ModalShell/Dialog, TextField, Select, Table, Pagination, …) |
| `components/skeletons/` | `SkeletonKit.tsx` – közös skeleton építőelemek |
| `components/brand/`, `components/charts/` | Logo, FinexCard, AuroraBackground; recharts tooltip |
| `session/` | `sessionStore` (token + lejárat a localStorage-ban: `finex.session`), `guards.tsx` (RequireAuth, GuestOnly, RequireAdmin), `SessionWatcher` |
| `theme/` | világos/sötét/rendszer téma (`finex.theme`), villanásmentes indítás a `public/theme-init.js`-ből, `themeTransition.ts` |
| `hooks/`, `lib/` | `useBankSettings`, `useMediaQuery`, …; `format.ts` (pénz, dátum, `SHY` lágy kötőjel), `dates.ts`, `iban.ts`, `idempotency.ts`, `topLayer.ts`, `motion.ts` |
| `test/` | Vitest beállítás (`setup.ts`); a tesztek a tesztelt fájl mellett vannak (`*.test.ts(x)`) |

Főbb minták:

- Szűrők, lapozás, kiválasztott elem **a címsorban** (`useSearchParams`), így linkelhetők (`?status=…&page=…`, `?tx=…`).
- Mutáció után a megfelelő `queryKeys` előtag érvénytelenítése (`invalidateQueries`).
- Űrlapok: react-hook-form + zod; a zod maximumok a DTO-validációt tükrözik.
- Pénzmozgató kérések (utalás, devizaváltás, kártyás fizetés) `Idempotency-Key` fejléccel (`lib/idempotency.ts`).
- Toastok (sonner) egy `popover="manual"` rétegben, amelyet minden `showModal` után a `lib/topLayer.ts` újra a
  legfelső rétegbe emel – így a toast a dialógus fölött látszik.
- Kódstílus: 2 szóköz, szimpla idézőjel, pontosvessző nélkül; oldalak `export default function`, hookok nevesített
  exporttal; kommentek ugyanúgy, mint a backendben (egysoros, rövid, magyar); az importok ábécérendben.
- A lágy kötőjel (U+00AD) a kódban a `SHY` konstansból jön (`lib/format.ts`), nem nyers karakterként.

### Design-elvek

- **Stílus:** teljes képernyős lila aurora háttér, üvegkártyák (`cardSurface`), lebegő 3D FineX kártya, parallax,
  lépcsőzetes belépő animációk, szavanként elmosódásból előtűnő címek. Sötét lila oldalsáv mindkét témában; minden
  `PageHeader` mögött aurora sáv, amely az átlátszó, görgetéskor lila üvegre váltó TopBar alá fut. A fejléc-sávon
  lévő gombok `glass` variánsúak. A mobilos mockupok 1:1 másolása (50/50 osztott belépőoldal) asztalon üres
  területeket hagyott, ezért nem ez lett a megoldás.
- **Szimmetria:** oldalanként egy oszloprendszer (pl. 3 oszlop `lg:col-span-2` darabokkal, vagy 4 oszlopos KPI-sor
  alatt 1:1 sorok), mindenhol ugyanaz a köz (`lg:gap-6`), egy sorban egyforma magasság (`flex flex-col` + `mt-auto`),
  hosszú listák levágva (pl. top 6 + „Mind a N” gomb), összesítő csempék `auto-fit` ráccsal. Ellenőrzés:
  kártyaszélek és -magasságok 1280 és 1024 px-en.
- **Nincs kitalált adat:** minden az adatbázisból jön; a belépőoldal funkciókat mutat, nem kitalált tranzakciókat;
  a `FinexCard`-nak nincs alapértelmezett neve/száma. Az üzleti szabályok a `GET /settings`-ből (`useBankSettings`).
- **Skeleton betöltés:** pörgő karika helyett az oldal elrendezését követő váz (route-onként `hydrateFallbackElement`);
  spinner csak gombban / kapcsolóban.
- 1920 → 320 px, világos és sötét mód. Nagy pénzösszegek konténer-lekérdezéses betűmérettel
  (`@container` + `text-[clamp(…,Ncqi,…)]`), nem törnek szám közben. Az 1024–1279 px tartományt (ikonsávos
  oldalsáv) külön is érdemes nézni. Hosszú egyszavas címkékhez lágy kötőjel (`SHY`).

---

## 5. Ellenőrzés és tesztelés

- **Frontend** (a `frontend` mappában): `npm test` (Vitest: 5 fájl, 21 teszt), `npm run lint`, `npm run build`
  (TypeScript-ellenőrzés + Vite build). A `src/test/setup.ts` kikapcsolja a Motion animációkat, és pótolja a jsdomból
  hiányzó API-kat (`matchMedia`, `ResizeObserver`, `getAnimations`, `scrollTo`).
- **Backend** (a `backend` mappában): `.\mvnw.cmd test` – Docker kell (Testcontainers). Várt eredmény:
  `Tests run: 512, Failures: 0, Errors: 0` → `BUILD SUCCESS`.
- **Docker Compose:** a `docker compose config` csak ellenőriz, semmit nem indít. A gyökér compose `postgres`
  service-e ugyanaz a konténer és volume, mint a `database` mappáé – egy `up` a fejlesztői adatbázison is lefuttatja a
  backend migrációit. Kipróbáláshoz ezért külön projektnevű compose ajánlott (saját adatbázissal, más porton, pl.
  `finex-e2e` / 3100), utána `docker compose down -v`.
- **Kézi felületi teszt:** 1280 / 1024 / 375 / 320 px, világos és sötét módban.

---

## 6. Ismert buktatók

**Backend / tesztek**
- A JPA auditing `@Profile("!test")`: a `@DataJpaTest` osztályok `@ActiveProfiles("test")`-et használnak, és a
  `createdAt`-et kézzel állítják. A `@SpringBootTest` osztályokhoz `@DirtiesContext` kell, mert az
  `AuditingEntityListener` statikus AspectJ aspektuson keresztül kapja a handlerét, és átszivárogna a későbbi kontextusokba.
- PostgreSQL nem tud típust adni egy null `Instant`-nak JPQL-ben (`:x is null`) → dátumtartományokhoz
  `DateUtils.BEGINNING_OF_TIME` / `END_OF_TIME`.
- CORS 403 egy másik porton futó frontendnél → `--finex.cors-allowed-origins=…` (a Vite proxyval normál esetben nincs CORS).
- `@WebMvcTest` + `IdempotencyService`: `@MockBean`, és `@BeforeEach`-ben a `execute(...)` hívja meg a kapott
  `Supplier`-t (különben a controller `null`-t ad vissza).
- A lapozott válasz szerkezete (`{ content, page: { size, number, totalElements, totalPages } }`) a
  `spring.data.web.pageable.serialization-mode=via-dto` tulajdonságból jön. **Ne** `@EnableSpringDataWebSupport`-tal
  legyen beállítva: az kikapcsolja a Spring Boot `SpringDataWebAutoConfiguration`-jét, és akkor a `max-page-size=100`
  sem érvényesül (2026-10-26-ig így volt: 2000 elemes oldalt is lehetett kérni).
- A JWT-hez kötött tesztekben a felhasználónak legyen `tokenVersion`-je (a `ver` claim nélküli token érvénytelen).

**Frontend**
- A recharts `ResponsiveContainer height="100%"`-nak **meghatározott magasságú szülő** kell: `h-60 grow` – a
  `min-h-60 flex-1` üres grafikont adott a Statisztika oldalon.
- Minden reszponzív rács `grid-cols-1` alappal kezdődjön (különben egy implicit auto oszlop a nowrap/truncate
  tartalommal kinő a képernyőből mobilon).
- A Tailwind nem látja a dinamikusan összerakott osztályneveket – mindig teljes literál kell.
- Opacitást az üvegelemen magán kell animálni, nem a szülőn (a szülő opacitása az animáció végéig elrontja a backdrop-blurt).
- JSX-komment (`{/* */}`) egy ternary ágának elején build-hibát okozott → oda sima `//` JS-komment kell.
- Egy szintaxishiba után a React Router gyorsítótárazza a sikertelen lazy importot – teljes újratöltés kell.
- A `hydrateFallbackElement` közül mindig a legmélyebb route-é érvényesül.
- Rejtett böngészőfülön a böngésző nem futtatja az animációs képkockákat, így a Motion-animációk ott állnak.

**Windows**
- Windows PowerShell 5.1-ben nincs `&&`, a parancsokat egyenként kell futtatni; az `npm.ps1` nem adja tovább a `--`
  utáni argumentumokat (helyette `npx …`).

---

## 7. Fejlesztői környezet

- JDK 21 (a projekt Java 17-re fordít), nincs globális Maven (a wrapper elég). Node.js 24 LTS, npm 11,
  Git (`core.autocrlf=true`), Docker Desktop.
- Eclipse + Spring Tools, Lombok javaagentként az `eclipse.ini`-ben; a projekt `backend` néven látszik, a
  `.metadata/` ignorálva van. A git-commit Eclipse-ben (Git Staging nézet) történik.
- Natív PostgreSQL ne fusson az 5432-es porton (ütközne a Dockeres adatbázissal).
- PowerShell ExecutionPolicy: `RemoteSigned` kell az `npm`-hez.
- A git push a Git Credential Managerrel működik.

---

## 8. Git-munkafolyamat

- Ágak: `main` és `main-mmark-dev`. Minden munka a `main-mmark-dev`-en folyik, a `main` ezt követi
  (fast-forward: `git push origin main-mmark-dev:main`, utána helyben `git fetch origin main:main`).
- Commit-üzenetek: Conventional Commits, angol tárgysor (≤ 72 karakter), logikai egységenként
  (pl. `feat(backend): …`, `feat(frontend): …`, `test(frontend): …`, `build: …`, `docs: …`).

---

## 9. Új funkció hozzáadásakor

1. Backend: a meglévő stílusban (3. fejezet), tesztekkel; sémaváltozás új Flyway-migrációban.
2. Frontend: a meglévő minták szerint (`PageHeader`, `Card`, skeleton, `queries.ts`, `queryKeys`, címsorban tartott
   szűrők), szimmetrikus rács, valós adat, a szabályok a `GET /settings`-ből.
3. Ellenőrzés (5. fejezet), majd a README funkciólistájának és ennek a dokumentációnak a frissítése.
