# FineX – adatbázis

A backend **PostgreSQL 15**-öt használ, Docker-konténerben.

## Indítás

A Docker Desktopnak futnia kell. Ebben a mappában:

```bash
docker compose up -d
```

(A teljes alkalmazás – adatbázis, backend, frontend – a gyökérben lévő `docker-compose.yml`-lel egy paranccsal is
indítható, ugyanezzel az adatbázissal: lásd [`docs/TELEPITES.md`](../docs/TELEPITES.md#8-indítás-egy-paranccsal-docker-compose).)

| Beállítás | Érték |
|---|---|
| Host, port | `localhost:5432` (csak erről a gépről érhető el: `127.0.0.1`) |
| Adatbázis | `finex` |
| Felhasználó / jelszó | `finex` / `finex123` (csak fejlesztői érték) |
| Konténer | `finex-postgres` |
| Adatok (volume) | `finex_finex-pgdata` |

> Ha a gépen natív PostgreSQL is fut, az ne az **5432**-es porton legyen (az eredeti fejlesztői gépen az 5433-ason
> fut, pgAdminnal), különben ütközik a konténerrel. Az alkalmazás csak a Dockeres adatbázist használja.

## Séma: Flyway-migrációk

A sémát nem a Hibernate generálja, hanem verziózott SQL-migrációk (**Flyway**), amelyeket a backend induláskor automatikusan lefuttat. A Hibernate csak ellenőrzi, hogy az entitások megfelelnek-e a sémának (`ddl-auto=validate`).

A fájlok helye: `backend/src/main/resources/db/`

| Fájl | Tartalom |
|---|---|
| `migration/V1__init_schema.sql` | Táblák, idegen kulcsok, `CHECK` megszorítások, indexek (köztük részleges indexek) |
| `migration/V2__ledger_rules.sql` | Trigger, amely tiltja a könyvelt tételek módosítását és törlését; `account_balance_at` és `account_statement` függvény (számlakivonat futó egyenleggel, ablakfüggvénnyel) |
| `migration/V3__reporting_views.sql` | `v_account_monthly_summary` nézet: havi bevétel/kiadás számlánként (`FILTER` záradék, budapesti idő) |
| `migration/V4__reference_data.sql` | Törzsadat: tranzakciókategóriák |
| `demo/V4_1__demo_data.sql` | Demóadatok (felhasználók, számlák, kártyák, fél év tranzakció, megtakarítások, értesítések, ticketek) |
| `migration/V5__security_hardening.sql` | `users.token_version` (a régi tokenek érvénytelenítése), `revoked_tokens` (kijelentkezett tokenek), a kártyabirtokos neve, kifejezés-alapú egyedi index (havonta legfeljebb egy kamatjóváírás), `savings_min_balance()` függvény (a havi minimumegyenleg a kamathoz), `standing_orders.execution_day` |
| `migration/V6__audit_log.sql` | `audit_log`: az admin műveletek naplója; trigger tiltja a módosítást és a törlést |
| `migration/V7__ledger_links_and_reversals.sql` | `related_transaction_id` (az utalás két lába), `reversal_of_id` (sztornó, egyedi: egy tétel csak egyszer sztornózható), trigger: a tétel pénzneme egyezzen a számláéval, a `balance_history` módosíthatatlan, `is_cashflow()` függvény (valódi bevétel/kiadás-e a tétel – a saját számlák közötti mozgás, a devizaváltás és a sztornó nem), a havi nézet ezt használja |
| `migration/V8__idempotency_keys.sql` | `idempotency_keys`: a már feldolgozott utalások / fizetések kulcsa és válasza (a dupla könyvelés ellen) |
| `migration/V9__reconciliation_views.sql` | `v_account_reconciliation`, `v_savings_reconciliation`: egyeztető nézetek – a tárolt egyenleg megegyezik-e a tételek összegével (`difference`), megtakarításnál az utolsó mozgás utáni egyenleggel (`balance_after`) is |

A lefuttatott migrációkat a `flyway_schema_history` tábla tartja nyilván. Egy már lefuttatott migrációs fájlt **nem szabad módosítani**; sémaváltozáshoz új fájl kell (`V10__...sql`).

Egyeztetés kézzel (pgAdminban vagy `psql`-ben) – üres eredmény = minden számla rendben:

```sql
SELECT * FROM v_account_reconciliation WHERE difference <> 0;
```

A tesztek (Testcontainers) csak a `migration` mappát futtatják, a demóadatokat nem.

## Demó felhasználók

| E-mail | Jelszó | Szerepkör |
|---|---|---|
| `anna@finex.hu` | `Demo1234!` | USER (forint- és euroszámla, kártyák, megtakarítások, rendszeres átutalás) |
| `bence@finex.hu` | `Demo1234!` | USER |
| `admin@finex.hu` | `Admin1234!` | ADMIN |

## Az adatbázis újraépítése

Ha tiszta lappal kell kezdeni (minden adat elvész, a demóadatok újra betöltődnek a backend következő indításakor):

```bash
docker compose down -v
docker compose up -d
```

## Régi adatok

A Flyway előtti, Hibernate által létrehozott régi adatbázisra nincs szükség. Ha a volume-ja (`finex_finex-db-data`) még megvan a gépen (`docker volume ls`), törölhető: `docker volume rm finex_finex-db-data`.
