# FineX – adatbázis

A backend **PostgreSQL 15**-öt használ, Docker-konténerben.

## Indítás

A Docker Desktopnak futnia kell. Ebben a mappában:

```bash
docker compose up -d
```

| Beállítás | Érték |
|---|---|
| Host, port | `localhost:5432` |
| Adatbázis | `finex` |
| Felhasználó / jelszó | `finex` / `finex123` (csak fejlesztői érték) |
| Konténer | `finex-postgres` |
| Adatok (volume) | `finex_finex-pgdata` |

> A gépen fut egy natív PostgreSQL 18 is az **5433**-as porton (pgAdminnal). Az alkalmazás azt nem használja.

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

A lefuttatott migrációkat a `flyway_schema_history` tábla tartja nyilván. Egy már lefuttatott migrációs fájlt **nem szabad módosítani**; sémaváltozáshoz új fájl kell (`V5__...sql`).

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

- A Flyway előtti, Hibernate által létrehozott adatbázis a **`finex_finex-db-data`** volume-ban megmaradt, a jelenlegi konténer nem használja. Ha már nincs rá szükség: `docker volume rm finex_finex-db-data`.
- A `dumps/finex-2026-01-14.backup` mentés (pgAdmin, custom formátum) szintén a régi sémához készült, az új sémába nem tölthető vissza, csak archívumként van meg.
