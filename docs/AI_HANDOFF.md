# FineX – részletes átadás AI-asszisztensnek

> Állapot: **2026-10-20**. A projekt gépet vált. Ez a fájl mindent tartalmaz, amit az eddigi AI-munkamenetek a régi
> gép saját memóriájában tartottak, hogy egy új munkamenet (más gépen, más AI-val is) innen folytathassa.
> A rövid, mindig betöltődő szabályok a gyökérben lévő [`CLAUDE.md`](../CLAUDE.md)-ben vannak; ez a hosszú változat.
> Telepítés és indítás új gépen: [`TELEPITES.md`](TELEPITES.md).

---

## 1. A felhasználó és a projekt

- **Ki a felhasználó:** Madarász Márk (GitHub: `MadaraszMark`), magyar anyanyelvű fejlesztő. **Mindig magyarul kommunikálj.**
  Windows 11-en dolgozik. A backendet Eclipse-ben (Spring Tools), a frontendet VS Code-ban fejleszti.
  Otthonosan mozog a Spring Bootban, JPA-ban, JWT-ben, Mockitóban, Testcontainersben; a React-et ezen a projekten tanulja.
- **Mi a projekt:** a FineX egy demó online bank, a felhasználó **BSc szakdolgozata** (webfejlesztés: Java Spring
  Boot + React). A dolgozatnak meg kell mutatnia a backend- és üzleti logikát, valamint adatbázis-ismeretet is
  (ezért van Flyway, trigger, SQL-függvény, nézet, részleges index).
- **Repó:** https://github.com/MadaraszMark/finex – **publikus**. Monorepo:

| Mappa | Tartalom |
|---|---|
| `backend/` | Spring Boot 3.5.8 REST API, Java 17 target (JDK 21-en fut), Maven wrapper (`mvnw.cmd`, Maven 3.9.11) |
| `frontend/` | React 19 + TypeScript + Vite 8 + Tailwind CSS 4 |
| `database/` | `docker-compose.yml` (PostgreSQL 15), a séma és a demóadatok leírása, régi dump |
| `docs/` | Ez a dokumentáció, telepítési útmutató, UI koncepcióképek (`images/` – iPhone/SwiftUI mockupok) |

---

## 2. Jelenlegi állapot (2026-10-20)

**Kész és GitHubon van** (`main` = `main-mmark-dev`):

- **Backend: teljes.** Felhasználók, regisztráció (automatikus folyószámla + kártya), JWT, USER/ADMIN szerepkör,
  számlák (HUF/EUR/USD, státuszok, kivonat futó egyenleggel), kártyák (tiltás, limitek, online fizetés, demó
  kártyás fizetés), utalás (napi limit, fedezet, párhuzamos utalásoknál is konzisztens), kategóriák,
  kedvezményezettek, rendszeres átutalások + ütemező, megtakarítások + kamatütemező, értesítések, statisztikák,
  ügyfélszolgálati ticketek üzenetváltással, belépési napló + zárolás 5 hibás próbálkozás után, admin API,
  `GET /settings` (üzleti szabályok a frontendnek). **431 automata teszt, mind zöld** (utolsó teljes futás: 2026-09-30).
- **Adatbázis:** Flyway V1–V4 + demóadat (V4_1), `ddl-auto=validate`.
- **Frontend: mind az 5 rész kész** (a terv 2026-09-29-én készült, 2026-09-30-ra elkészült):
  1. Alapok: design system, animált belépés/regisztráció, alkalmazáskeret, munkamenet, útvonalvédelem
  2. Főoldal, számlák, kártyák
  3. Tranzakciók, utalás-varázsló, kedvezményezettek, rendszeres átutalások
  4. Megtakarítások, statisztika
  5. Értesítések, profil, ügyfélszolgálat, teljes admin felület
  - Utána: aurora/üveg újratervezés az egész appon, szimmetrikus kártyarácsok, dummy adatok kiirtása,
    skeleton-betöltők minden oldalon, Ctrl+K parancspaletta, natív `<dialog>` alapú ablakok (mobilon lehúzható
    alsó lap), körben szétterülő témaváltás.
- Statikus ellenőrzés (tsc, eslint, vite build) zöld.

**Commit-előzmények:** `5083355` backend · `ef27afc` database · `a61a08e` frontend alapok · `718d2be` README
(2026-09-29), majd 2026-10-20-án a frontend 2–5. rész, a `/settings` backend és ez a dokumentáció.

### Nyitott kérdések / teendők

1. **Adatvédelmi kérdés – megválaszolatlan.** A `database/dumps/finex-2026-01-14.backup` (régi, Flyway előtti séma,
   pg_dump custom formátum) a publikus repóban van, és **valódi személyek adatait** tartalmazhatja (11 felhasználó).
   Meg kell kérdezni a felhasználót, hogy maradhat-e. **Kérdezés nélkül ne töröld.** Ha törölni kell: a fájl a
   git-előzményekben is benne van, tehát csak HEAD-ből törölni nem elég (előzmény-átírás kellene, ami force pusht
   jelent – ehhez külön, kifejezett engedély kell).
2. **GitHub „About” leírás** még a régi („mobile banking backend”). A felhasználónak kell átírnia a github.com-on
   (nincs `gh` CLI). Javaslat: „FineX – digitális bank: Spring Boot REST API + React webes felület (BSc szakdolgozat)”.
3. **A Welcome oldal** (`frontend/src/features/auth/pages/WelcomePage.tsx`) még a régi stílusú – ez az egyetlen
   oldal, ami nem kapta meg az aurora/üveg újratervezést.
4. A README „Tervezett funkciók” része: automatikus havi megtakarítás; tervezett SwiftUI mobilalkalmazás (csak koncepció).
5. A felhasználó a felületet maga nézi át; ha visszajelzést ad (képernyőképpel), azt kell javítani.

---

## 3. Backend architektúra

Csomag: `hu.finex.main` (`backend/src/main/java/hu/finex/main/`)

| Csomag | Szerep |
|---|---|
| `controller/` | REST végpontok, Swagger-annotációkkal. Admin végpontok: `Admin*Controller`, útvonal `/admin/**` (`hasRole("ADMIN")`) |
| `service/` | Üzleti logika. `LedgerService`: **minden egyenlegváltozás** ezen megy át → tranzakció + `balance_history` sor |
| `repository/` | Spring Data JPA; zárolás: `@Lock(PESSIMISTIC_WRITE)` (számla, megtakarítás), mindig **id szerinti sorrendben** (holtpont ellen) |
| `model/`, `model/enums/` | JPA entitások (Lombok builder) |
| `dto/` | Request/Response osztályok, Jakarta Validation + `@Schema` |
| `mapper/` | Kézzel írt `@Component` mapperek (nincs MapStruct) |
| `security/` | `JwtAuthenticationFilter`, `JwtTokenUtil`, `CurrentUser` (a principal = felhasználó id; `requireId()`, `requireEntity()`, `ensureSameUser()`) |
| `config/` | `SecurityConfig` (stateless, CORS), `FinexProperties` (`finex.*` üzleti szabályok), `JpaAuditingConfig` (`@Profile("!test")`), `OpenApiConfig` |
| `scheduler/` | `StandingOrderScheduler` (óránként, :05), `SavingsInterestScheduler` (óránként, :15; havonta egyszer ír jóvá) – az admin kézzel is indíthatja |
| `exception/` | `BusinessException` (400), `NotFoundException` (404), `GlobalExceptionHandler` → `ApiError` |
| `util/` | `DateUtils`, `IbanUtils`, `MoneyUtils` |

- **Jogosultság:** minden felhasználó csak a saját adatait látja: a service-ekben `findOwned(id)` minta
  (`repository.findByIdAndUser_Id(id, currentUser.requireId())` → `NotFoundException`).
- **Konfiguráció** (`application.properties`): környezeti változók alapértékkel – `FINEX_DB_URL`
  (`jdbc:postgresql://localhost:5432/finex`), `FINEX_DB_USERNAME` (`finex`), `FINEX_DB_PASSWORD` (`finex123`),
  `FINEX_JWT_SECRET`. JWT lejárat 1 óra. `finex.cors-allowed-origins=http://localhost:5173`.
  Üzleti szabályok: `finex.transfer-daily-limit`, `finex.card-default-daily-limit(-foreign)`,
  `finex.savings-interest-rate`, `finex.max-accounts-per-user`, `finex.login-max-failed-attempts`, `finex.login-lock-minutes`.
- **`GET /settings`** (`BankSettingsController` → `BankSettingsService` → `BankSettingsResponse`): a frontend innen
  olvassa a napi utalási limitet, a max. számlaszámot, a kamatlábat és a kártyalimiteket.

### Backend kódstílus – SZIGORÚAN így kell (a felhasználó kifejezett kérése)

A meglévő kódot kell utánozni, például: `controller/BeneficiaryController.java`, `service/BeneficiaryService.java`,
`mapper/BeneficiaryMapper.java`, `dto/BeneficiaryResponse.java`.

- 4 szóközös behúzás; Lombok `@RequiredArgsConstructor` + `private final` mezők.
- Controller: `ResponseEntity<…>`, `@Tag(name = "… API", description = "…")`, és minden végponton
  `@Operation(summary = "…",responses = { @ApiResponse(responseCode = "…", description = "…") })` – **pont ebben a
  formában** (vessző után szóköz nélkül a `responses` előtt, a záró `}` és `)` külön sorban, ahogy a meglévő fájlokban).
  `@PathVariable("id") Long id` – explicit névvel. Létrehozás: `ResponseEntity.status(HttpStatus.CREATED).body(…)`,
  törlés: `ResponseEntity.noContent().build()`.
- DTO: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` + `@Schema(description = "…")` az osztályon,
  minden mezőn `@Schema(description, example)`, `import lombok.*;`.
- Service: olvasásnál `@Transactional(readOnly = true)`, írásnál `@Transactional`; magyar hibaüzenetek
  `BusinessException` / `NotFoundException`-nel; privát segédmetódusok a végén.
- Mapper: `toEntity`, `updateEntity`, `toResponse` builderrel.
- Repository: származtatott lekérdezésnevek (`findByUser_IdOrderByNameAsc`).
- Kommentek: magyarul, ritkán, egysoros `//` – a „miért”-et magyarázzák.
- Tesztek mintája: `src/test/java/hu/finex/main/` – mapper unit, Mockito service, `@WebMvcTest` controller,
  `@DataJpaTest` Testcontainers PostgreSQL-lel (`repository/PostgresRepositoryTestBase.java`), párhuzamossági
  integrációs teszt (`service/TransferConcurrencyIntegrationTest.java`).

### Adatbázis

- PostgreSQL 15 Dockerben (`database/docker-compose.yml`, projektnév `finex`, konténer `finex-postgres`, volume
  `finex_finex-pgdata`). Részletek: [`database/README.md`](../database/README.md).
- **Flyway**: `backend/src/main/resources/db/migration/V1…V4` + `db/demo/V4_1__demo_data.sql`. Lefuttatott
  migrációt **tilos módosítani** (checksum hiba) – sémaváltozáshoz új fájl kell: `V5__….sql`.
- Bemutató elemek: `CHECK` megszorítások, részleges unique index, a könyvelt tételeket védő trigger,
  `account_balance_at` / `account_statement` függvények (ablakfüggvény), `v_account_monthly_summary` nézet.
- A tesztek (Testcontainers, `postgres:15-alpine`) csak a `migration` mappát futtatják, a demóadatot nem.
- A régi dump pg_dump 18 custom archívum – csak pg_restore ≥ 17 olvassa, és a régi sémához tartozik (nem tölthető az újba).

---

## 4. Frontend architektúra

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
| `theme/` | világos/sötét/rendszer téma (`finex.theme`), villanásmentes script az `index.html`-ben, `themeTransition.ts` |
| `hooks/`, `lib/` | `useBankSettings`, `useMediaQuery`, …; `format.ts` (pénz, dátum, `SHY` lágy kötőjel), `dates.ts`, `iban.ts`, `topLayer.ts`, `motion.ts` |

Főbb minták:

- Szűrők, lapozás, kiválasztott elem **a címsorban** (`useSearchParams`), így linkelhetők (`?status=…&page=…`, `?tx=…`).
- Mutáció után a megfelelő `queryKeys` előtag érvénytelenítése (`invalidateQueries`).
- Űrlapok: react-hook-form + zod; a zod maximumok a DTO-validációt tükrözik (ez API-szerződés, nem dummy adat).
- Toastok (sonner) egy `popover="manual"` rétegben, amelyet minden `showModal` után a `lib/topLayer.ts` újra a
  legfelső rétegbe emel – így a toast a dialógus fölött látszik.
- Kódstílus: 2 szóköz, szimpla idézőjel, pontosvessző nélkül; oldalak `export default function`, hookok nevesített
  exporttal; magyar kommentek a „miért”-ről; az importok ábécérendben.

### Design-elvárások (a felhasználó visszajelzései alapján)

- **Stílus:** teljes képernyős lila aurora háttér, üvegkártyák (`cardSurface`), lebegő 3D FineX kártya, parallax,
  lépcsőzetes belépő animációk, szavanként elmosódásból előtűnő címek. Sötét lila oldalsáv mindkét témában; minden
  `PageHeader` mögött aurora sáv, amely az átlátszó, görgetéskor lila üvegre váltó TopBar alá fut. A fejléc-sávon
  lévő gombok `glass` variánsúak. (Az első, 50/50 osztott belépőoldalt a felhasználó „szörnyűnek” tartotta – a
  mobilos mockupok 1:1 másolása asztalon üres területeket hagy.)
- **Szimmetria** (a felhasználó panasza 2026-09-30: „a kártyák nem szimmetrikusak … ez több oldalra is igaz”):
  oldalanként egy oszloprendszer (pl. 3 oszlop `lg:col-span-2` darabokkal, vagy 4 oszlopos KPI-sor alatt 1:1 sorok),
  mindenhol ugyanaz a köz (`lg:gap-6`), egy sorban egyforma magasság (`flex flex-col` + `mt-auto`), hosszú listák
  levágva (pl. top 6 + „Mind a N” gomb), összesítő csempék `auto-fit` ráccsal. Ellenőrzés: kártyaszélek és
  -magasságok 1280 és 1024 px-en.
- **Nincs kitalált adat** (2026-09-30: „dumy adatok ne legyenek, minden jöjjön adatbázisból”): a belépőoldal
  funkciókat mutat, nem kitalált tranzakciókat; a `FinexCard`-nak nincs alapértelmezett neve/száma.
- **Skeleton betöltés** (2026-09-30: „ne spinning karika legyen, hanem skeleton loaders that match the layout”):
  a `PageLoader` és a `SplashScreen` törölve; a gyökér tartalék üres háttér.
- 1920 → 320 px, világos és sötét mód. Nagy pénzösszegek konténer-lekérdezéses betűmérettel
  (`@container` + `text-[clamp(…,Ncqi,…)]`), nem törnek szám közben. Az 1024–1279 px tartományt (ikonsávos
  oldalsáv) külön is nézd. Hosszú egyszavas címkékhez lágy kötőjel (`SHY` a `lib/format.ts`-ből).

---

## 5. Ellenőrzés és tesztelés (AI-munkamenethez)

- **Alapértelmezés: csak statikus frontend-ellenőrzés.** Hogy a felhasználó futó dev szerverét és `node_modules`-át ne
  zavard, az eredeti gépen egy ideiglenes másolatban futott: `robocopy frontend <scratch>\frontend-build /E /XD
  node_modules dist`, ott `npm install`, majd
  `npx tsc -b`, `npx eslint .`, `npx vite build`.
- **Backend:** csak ha backend kód változott: másolat (`/XD target .metadata .settings`), `mvnw.cmd -B test`.
  Docker kell (Testcontainers). Várt eredmény: `Tests run: 431, Failures: 0, Errors: 0` → `BUILD SUCCESS`.
- **Részletes teszt csak kifejezett kérésre** („mehet egy részletes teszt”): saját ideiglenes PostgreSQL konténer
  (pl. 55432-es port) + backend jar (pl. 18080, `--finex.cors-allowed-origins=http://localhost:5174`) + Vite a
  5174-es porton + böngészős bejárás (1280/1024/375/320, világos+sötét). Utána **mindent állíts le és törölj**
  (`docker rm -fv` az ideiglenes konténerre). Ne indíts alkalmazást a felhasználó gépén kérés nélkül (egyszer a
  Docker Desktop elindítása miatt szólt rá az AI-ra).

---

## 6. Ismert buktatók

**Backend / tesztek**
- A JPA auditing `@Profile("!test")`: a `@DataJpaTest` osztályok `@ActiveProfiles("test")`-et használnak, és a
  `createdAt`-et kézzel állítják. A `@SpringBootTest` osztályokhoz `@DirtiesContext` kell, mert az
  `AuditingEntityListener` statikus AspectJ aspektuson keresztül kapja a handlerét, és átszivárogna a későbbi kontextusokba.
- PostgreSQL nem tud típust adni egy null `Instant`-nak JPQL-ben (`:x is null`) → dátumtartományokhoz
  `DateUtils.BEGINNING_OF_TIME` / `END_OF_TIME`.
- CORS 403 egy másik porton futó frontendnél → `--finex.cors-allowed-origins=…` (a Vite proxyval normál esetben nincs CORS).

**Frontend**
- A recharts `ResponsiveContainer height="100%"`-nak **meghatározott magasságú szülő** kell: `h-60 grow` – a
  `min-h-60 flex-1` üres grafikont adott a Statisztika oldalon.
- Minden reszponzív rács `grid-cols-1` alappal kezdődjön (különben egy implicit auto oszlop a nowrap/truncate
  tartalommal kinő a képernyőből mobilon).
- A Tailwind nem látja a dinamikusan összerakott osztályneveket – mindig teljes literál kell.
- Opacitást az üvegelemen magán animálj, ne a szülőn (a szülő opacitása az animáció végéig elrontja a backdrop-blurt).
- JSX-komment (`{/* */}`) egy ternary ágának elején build-hibát okozott → oda sima `//` JS-komment kell.
- Egy szintaxishiba után a React Router gyorsítótárazza a sikertelen lazy importot – teljes újratöltés kell.
- A `hydrateFallbackElement` közül mindig a legmélyebb route-é érvényesül.

**AI-eszközök (Claude Code, Windows)**
- A lágy kötőjel (U+00AD) az Edit/Write eszközben nyers karakterré válik → a kódban `String.fromCharCode(0xad)` /
  a meglévő `SHY` konstans.
- Bash-ban `node -e "…"` scriptben a backtick és a `${}` elvész → template literálokat Edit eszközzel szerkessz.
- Lehet, hogy az AI shelljének PATH-ján nincs rajta a Node – ilyenkor elé kell tenni: `C:\Program Files\nodejs`.
- PowerShell 5.1: nincs `&&`; az `npm.ps1` lenyeli a `--` argumentum-továbbítást. A felhasználónak adott
  parancsok ezért egyenként, külön kódblokkban.
- A beépített böngészőpanel rejtve lassítja az animációkat (rAF); a képernyőkép egy renderrel lemaradhat – JS-sel
  ellenőrizz, vagy kapcsold ki az animációkat az ideiglenes másolatban (`MotionGlobalConfig.skipAnimations`).

---

## 7. A régi fejlesztői gép (tájékoztatásul)

- Java 21.0.9 (Oracle) a PATH-on, `JAVA_HOME` nincs beállítva; nincs globális Maven (a wrapper elég).
  Node.js 24.19, npm 11.17, Git 2.45 (`core.autocrlf=true`), Docker Desktop 29.
- Eclipse JEE 2022-12 + Spring Tools 4.29.1, Lombok javaagentként az `eclipse.ini`-ben; a workspace a FineX gyökér
  volt (a projekt `backend` néven látszik; a `.metadata/` ignorálva van).
- A régi gépen futott egy natív PostgreSQL 18 is az 5433-as porton pgAdminnal – az alkalmazás nem használja.
- PowerShell ExecutionPolicy: `RemoteSigned` kellett az `npm`-hez.
- A git push a Git Credential Managerrel működik; `gh` CLI nincs.

---

## 8. Hogyan kezdj neki egy új munkamenetben

1. Olvasd el a `CLAUDE.md`-t és ezt a fájlt; a funkciólistához a gyökér `README.md`-t, a sémához a `database/README.md`-t.
2. `git status` és `git log --oneline -5` – nézd meg, van-e félbehagyott munka.
3. Kérdezd meg a felhasználót, mivel folytatná; a nyitott kérdéseket (2. fejezet) egyszer említsd meg.
4. Új funkciónál: backend a meglévő stílusban + teszt; frontend a meglévő minták szerint (`PageHeader`, `Card`,
   skeleton, `queries.ts`, `queryKeys`, URL-ben tartott szűrők), szimmetrikus rács, valós adat.
5. A végén: statikus ellenőrzés, rövid magyar összefoglaló arról, mit nézzen meg a felhasználó, és egy
   Conventional Commit üzenetjavaslat (commit csak kérésre).
