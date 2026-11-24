# FineX: indítás és használat

A FineX egy demó online bank: Spring Boot REST API (`backend/`), React webes felület (`frontend/`) és PostgreSQL
adatbázis (`database/`). Ez a leírás azt mutatja meg, hogyan indítható el az alkalmazás egyetlen paranccsal, és
mit érdemes kipróbálni benne. A fejlesztői környezet (Eclipse, VS Code, tesztek) leírása a
[`docs/TELEPITES.md`](docs/TELEPITES.md) fájlban van.

## 1. Amire szükség van

- **Docker Desktop** (Windows, macOS) vagy Docker Engine a Compose bővítménnyel (Linux). Java és Node.js nem kell,
  mindent a Docker fordít le.
- Szabad **3000**-es és **5432**-es port a gépen.
- Az első indításhoz internetkapcsolat (a Docker letölti az alapképeket és a függőségeket).

## 2. Indítás

1. Csomagold ki a ZIP-et, és indítsd el a Docker Desktopot. Várd meg, amíg az „Engine running” állapot megjelenik.
2. Nyiss egy terminált (Windowson PowerShell) a kicsomagolt `FineX` mappában. Például:

```powershell
cd C:\Users\<felhasználónév>\Downloads\FineX
```

3. Indítsd el a teljes alkalmazást:

```powershell
docker compose up -d --build --wait
```

Az első indítás 5–10 percig tart, mert a Docker lefordítja a backendet és a frontendet. Ha minden rendben van,
a végén ez látszik:

```
✔ Container finex-postgres  Healthy
✔ Container finex-backend   Healthy
✔ Container finex-frontend  Healthy
```

4. Nyisd meg a böngészőben: **http://localhost:3000** (http, nem https).

Az API leírása (Swagger UI): http://localhost:3000/api/swagger-ui.html. A végpontok listája a
[`docs/API.md`](docs/API.md) fájlban is megtalálható.

## 3. Belépés

Az adatbázis induláskor demóadatokkal töltődik fel: fél év tranzakció, kártyák, megtakarítások, értesítések,
ügyfélszolgálati ticketek.

| E-mail | Jelszó | Szerepkör |
|---|---|---|
| `anna@finex.hu` | `Demo1234!` | ügyfél (forint- és euroszámla, kártyák, megtakarítások, rendszeres átutalás) |
| `bence@finex.hu` | `Demo1234!` | ügyfél |
| `admin@finex.hu` | `Admin1234!` | adminisztrátor |

Új fiók a Regisztráció oldalon is létrehozható, ilyenkor a rendszer automatikusan nyit egy folyószámlát és egy
bankkártyát.

Két biztonsági szabályra érdemes figyelni kipróbálás közben. Ha ugyanarra a fiókra ugyanarról a gépről ötször
hibás jelszót adnak meg, a belépés 15 percre zárolódik, és egy gépről percenként legfeljebb 10 belépési kísérlet
engedélyezett.

## 4. Mit érdemes kipróbálni

A legszemléletesebb, ha két böngészőablakot használsz: egy normál ablakban Annaként, egy inkognitóablakban
Bencéként lépsz be. A két ablak külön munkamenetet tart, így egyszerre két felhasználó lehet bejelentkezve.

**Ügyfélként (Anna):**

- **Főoldal:** összesített egyenleg, havi bevétel és kiadás grafikonon, legutóbbi tételek. A fejléc
  profilmenüjében (és a Profil és biztonság oldalon) válthatsz világos és sötét mód között. A **Ctrl + K** megnyitja a parancspalettát.
- **Számlák:** egy számlára kattintva megjelenik az egyenleggrafikon és a kivonat futó egyenleggel. A kivonat
  CSV-ben letölthető. Itt indítható a devizaváltás is a saját számlák között.
- **Kártyák:** tiltás és feloldás, napi limit, online fizetés engedélyezése, demó kártyás fizetés.
- **Utalás:** a varázslóban válaszd a mentett **Nagy Bence** kedvezményezettet, adj meg egy összeget, és haladj
  végig a lépéseken. A végén nyugta jelenik meg. Bence ablakában megjelenik az értesítés a beérkezett összegről,
  az olvasatlan értesítések száma pedig a böngészőfül címében is látszik.
- **Tranzakciók:** keresés és szűrés időszak, típus, összeg és kategória szerint; egy tételre kattintva megnyílik
  az adatlapja.
- **Rendszeres átutalások, Megtakarítások, Statisztika, Értesítések, Ügyfélszolgálat, Profil és biztonság**
  (itt láthatók a belépési előzmények is).

**Adminisztrátorként:**

- **Admin áttekintés:** a bank fő számai; az ütemezett feladatok (rendszeres átutalások, havi kamatjóváírás) a
  **Futtatás** gombbal azonnal elindíthatók.
- **Felhasználók:** ha letiltasz egy éppen bejelentkezett ügyfelet, a következő kérésénél azonnal kijelentkezteti a
  rendszer. Utána a tiltás feloldható.
- **Számlák kezelése:** készpénzbefizetés (ügyfél nem írhat jóvá magának pénzt), a **Tételek** gomb alatt sztornó.
  A sztornó nem törli a tételt, hanem ellentétes tételt könyvel.
- **Ticketek, Belépési napló, Audit napló** (minden admin művelet ide kerül, utólag nem módosítható),
  **Kategóriák**.

A felület mobilnézetben is kipróbálható: a böngészőben F12, majd a telefon ikonnal válthatsz mobil szélességre.

## 5. Az adatbázis megtekintése (nem kötelező)

Az adatbázis csak erről a gépről érhető el. Bármilyen PostgreSQL-klienssel (pgAdmin, DBeaver) csatlakozhatsz:

| Beállítás | Érték |
|---|---|
| Host | `127.0.0.1` |
| Port | `5432` |
| Adatbázis | `finex` |
| Felhasználó / jelszó | `finex` / `finex123` |

Windowson a hostnál `127.0.0.1`-et adj meg, ne `localhost`-ot. A `localhost` esetén a Windows először IPv6-on
próbálkozik, és minden kapcsolat kb. 2 másodperccel lassabb lesz.

Kliens nélkül, parancssorból is belenézhetsz:

```powershell
docker exec -it finex-postgres psql -U finex -d finex
```

Hasznos lekérdezések: `\dt` (táblák), `SELECT * FROM flyway_schema_history;` (a lefutott migrációk),
`SELECT * FROM v_account_reconciliation WHERE difference <> 0;` (üres eredmény: minden számla egyenlege egyezik a
tételek összegével). Kilépés: `\q`. A séma részletes leírása a [`database/README.md`](database/README.md) fájlban van.

## 6. Leállítás

A `FineX` mappában:

```powershell
docker compose down
```

Az adatok megmaradnak, a következő `docker compose up -d --build --wait` ugyanott folytatja (másodpercek alatt).
Ha tiszta lappal szeretnéd újrakezdeni, a `docker compose down -v` az adatbázist is törli, és a következő
indításkor a demóadatok újra betöltődnek.

## 7. Hibaelhárítás

| Jelenség | Megoldás |
|---|---|
| `error during connect` vagy `Cannot connect to the Docker daemon` | Nem fut a Docker Desktop. Indítsd el, várd meg az „Engine running” állapotot. |
| `port is already allocated` (3000 vagy 5432) | Egy másik program foglalja a portot (gyakran egy helyben telepített PostgreSQL az 5432-t). Állítsd le, vagy írd át a portot: a 3000-et a gyökérben lévő `docker-compose.yml`-ben, az 5432 bal oldali számát a `database/docker-compose.yml`-ben (például `"127.0.0.1:5433:5432"`). Az alkalmazás belül továbbra is működik, csak az adatbázis-kliensnek kell az új portot megadni. |
| A `--wait` hibával kilép (`container finex-backend is unhealthy`) | `docker compose logs backend` mutatja az okot. |
| „A webhely nem érhető el” | A cím **http://localhost:3000**, nem https, és nem az 5173-as port (az csak fejlesztői módban él). |
| Újraindítás után kiléptet | Ez várt működés: JWT-kulcs nélkül minden indításkor új, véletlen kulcs készül. Állandó kulcshoz másold le a `.env.example` fájlt `.env` néven, és írj bele legalább 32 karakteres kulcsot. |
| „Túl sok sikertelen bejelentkezési kísérlet” | Öt hibás jelszó után 15 perc zárolás jár. Várj, vagy lépj be másik demófelhasználóval. |
| „Túl sok bejelentkezési kérés” (429) | Percenként legfeljebb 10 belépés engedélyezett, várj egy percet. |

## 8. A mappák tartalma

| Mappa | Tartalom |
|---|---|
| `backend/` | Spring Boot REST API (Java 17, Maven), a Flyway-migrációk (`src/main/resources/db`) és az automata tesztek (`src/test`) |
| `frontend/` | React + TypeScript webes felület (Vite, Tailwind CSS), Vitest tesztek, nginx-beállítás |
| `database/` | A PostgreSQL konténer leírása, a séma és a demóadatok dokumentációja |
| `docs/` | Telepítési útmutató, fejlesztői dokumentáció, a REST API végpontjai, UI koncepcióképek |
| `docker-compose.yml` | A teljes alkalmazás egy paranccsal (adatbázis, backend, nginx-szel kiszolgált frontend) |

A tesztek futtatása (backend: 512 teszt, frontend: 21 teszt) és a fejlesztői indítás Docker nélküli Javával és
Node.js-szel a [`docs/TELEPITES.md`](docs/TELEPITES.md) fájlban szerepel.
