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
| Adatok (volume) | `finex_finex-db-data` |

A táblákat a backend hozza létre induláskor (Hibernate, `ddl-auto=update`), külön sémaszkript nincs.

> A gépen fut egy natív PostgreSQL 18 is az **5433**-as porton (pgAdminnal). Az alkalmazás azt nem használja.

## Mentés: `dumps/finex-2026-01-14.backup`

A fájl pgAdminnal (pg_dump 18) készült, **custom formátumú** mentés, nem sima SQL, ezért `psql`-lel nem futtatható.
A visszatöltéshez `pg_restore` 17 vagy újabb kell (a konténer PG 15-ös `pg_restore`-ja nem tudja beolvasni). Használható például a natív PostgreSQL 18-é, üres `finex` adatbázisba, még a backend első indítása előtt:

PowerShellben, ebből a mappából (a jelszó `finex123`):

```powershell
& "C:\Program Files\PostgreSQL\18\bin\pg_restore.exe" -h localhost -p 5432 -U finex -d finex dumps\finex-2026-01-14.backup
```

Ha a visszatöltés a `transaction_timeout` beállításra panaszkodik, az nyugodtan figyelmen kívül hagyható: ezt a PG 15 még nem ismeri.
