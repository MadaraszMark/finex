# FineX – telepítés és indítás új gépen

Lépésről lépésre, egy üres Windows 11-es gépről indulva. Minden parancs **PowerShellben** fut (VS Code terminál vagy
Windows Terminal). A parancsokat egyenként másold be, és nézd meg, hogy a „✅ Ha jó” sorban leírt kimenetet kapod-e.

**Összesen kb. 45–60 perc**, ennek nagy része letöltés.

---

## 1. Letöltendő programok

| Program | Mire kell | Verzió | Kötelező? |
|---|---|---|---|
| **Git** | a kód letöltése és verziókezelése | legújabb | ✅ |
| **JDK 21** (Eclipse Temurin) | a backend fordítása és futtatása | 21 LTS (a projekt Java 17-re fordít, 17+ elég) | ✅ |
| **Node.js** | a frontend | 24 LTS (legalább 22.12) | ✅ |
| **Docker Desktop** | a PostgreSQL adatbázis és a backend tesztjei | legújabb | ✅ |
| **VS Code** | frontend fejlesztés | legújabb | ✅ |
| **Eclipse + Spring Tools + Lombok** | backend fejlesztés | legújabb | ajánlott (a backend Eclipse nélkül is fut) |
| **pgAdmin 4** vagy **DBeaver** | adatbázis böngészése | legújabb | opcionális |
| **Claude asztali app** (Claude Code) | az AI-asszisztens | legújabb | opcionális |

> Maven-t **nem** kell telepíteni: a `backend` mappában lévő `mvnw.cmd` (Maven wrapper) első futáskor letölti.
> **Natív PostgreSQL-t se telepíts** (az ütközne a Dockeres adatbázissal az 5432-es porton).

### 1.1 Telepítés winget-tel (a leggyorsabb)

Nyiss egy PowerShellt **rendszergazdaként** (Start → „PowerShell” → jobb klikk → Futtatás rendszergazdaként).
Egyenként futtasd:

```powershell
winget install --id Git.Git -e
```

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK -e
```

```powershell
winget install --id OpenJS.NodeJS.LTS -e
```

```powershell
winget install --id Docker.DockerDesktop -e
```

```powershell
winget install --id Microsoft.VisualStudioCode -e
```

✅ Ha jó: mindegyik végén `Successfully installed` jelenik meg.

Ha a winget nem működik, kézzel is letölthetők:
[git-scm.com](https://git-scm.com/download/win) ·
[adoptium.net](https://adoptium.net/temurin/releases/?version=21) (Windows x64, JDK, `.msi`) ·
[nodejs.org](https://nodejs.org/) (LTS) ·
[docker.com](https://www.docker.com/products/docker-desktop/) ·
[code.visualstudio.com](https://code.visualstudio.com/).

### 1.2 Docker Desktop első indítása

1. **Indítsd újra a gépet** (a Docker a WSL 2-t használja, ez újraindítás után lesz kész).
2. Indítsd el a Docker Desktopot, fogadd el a feltételeket; bejelentkezni nem kell (Skip).
3. Ha azt írja, hogy a WSL frissítésre szorul, egy rendszergazdai PowerShellben:

```powershell
wsl --update
```

✅ Ha jó: a Docker Desktop bal alsó sarkában zöld „Engine running” felirat.

Javasolt: Docker Desktop → Settings → General → „Start Docker Desktop when you sign in” bekapcsolva.

### 1.3 Eclipse a backendhez (ajánlott)

1. Töltsd le a **Spring Tools for Eclipse**-t: https://spring.io/tools (Windows x86_64), csomagold ki például a
   `C:\Tools\` mappába. (Másik lehetőség: „Eclipse IDE for Enterprise Java and Web Developers” az eclipse.org-ról,
   majd Help → Eclipse Marketplace → „Spring Tools”.)
2. **Lombok** (enélkül az Eclipse hibát jelez a getterekre): töltsd le a `lombok.jar`-t a https://projectlombok.org/download
   oldalról, majd kattints rá duplán, vagy PowerShellben abban a mappában:

```powershell
java -jar lombok.jar
```

   A megnyíló ablakban: „Specify location…” → válaszd ki az Eclipse mappáját (ahol az `.exe` és az `.ini` van) →
   **Install / Update** → Quit. Utána indítsd (újra) az Eclipse-t.

### 1.4 VS Code bővítmények

```powershell
code --install-extension dbaeumer.vscode-eslint
```

```powershell
code --install-extension bradlc.vscode-tailwindcss
```

---

## 2. Ellenőrzés és alapbeállítások

**Zárd be és nyisd meg újra a PowerShellt / VS Code-ot** (különben nem látja az új programokat), majd:

```powershell
git --version
```
✅ Ha jó: `git version 2.…`

```powershell
java -version
```
✅ Ha jó: `openjdk version "21.0.…"`

```powershell
node -v
```
✅ Ha jó: `v24.…` (vagy újabb LTS)

```powershell
docker --version
```
✅ Ha jó: `Docker version …`

### 2.1 PowerShell: npm engedélyezése

Alapból a Windows letiltja az `npm` szkriptjét („running scripts is disabled on this system”). Egyszer kell, sima
(nem rendszergazdai) PowerShellben:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

Ha kérdez, írd be: `Y`. Utána:

```powershell
npm -v
```
✅ Ha jó: egy verziószám, pl. `11.…`

### 2.2 Git név és e-mail

```powershell
git config --global user.name "MadaraszMark"
```

```powershell
git config --global user.email "A-GITHUBON-HASZNALT-EMAIL-CIMED"
```

```powershell
git config --global core.autocrlf true
```

---

## 3. A projekt letöltése

Például az Asztalra (a mappa neve legyen `FineX`, mint eddig):

```powershell
cd $HOME\Desktop
```

```powershell
git clone https://github.com/MadaraszMark/finex.git FineX
```

```powershell
cd FineX
```

```powershell
git switch main-mmark-dev
```

✅ Ha jó: `Switched to a new branch 'main-mmark-dev'` és `branch 'main-mmark-dev' set up to track 'origin/main-mmark-dev'`.

> Az első `git push`-nál felugrik a GitHub bejelentkezés (Git Credential Manager) – egyszer kell jóváhagyni a böngészőben.

A munka mindig a `main-mmark-dev` ágon folyik; a `main` ezt követi.

---

## 4. Indítás – első alkalommal

Három dolog fut egyszerre: **adatbázis (Docker) → backend (8080) → frontend (5173)**. Ebben a sorrendben indítsd.

### 4.1 Adatbázis

A Docker Desktopnak futnia kell. A `FineX` mappából:

```powershell
cd database
```

```powershell
docker compose up -d
```

✅ Ha jó (első alkalommal letölti a `postgres:15` képet, ez 1–2 perc):
```
✔ Network finex_default         Created
✔ Volume "finex_finex-pgdata"   Created
✔ Container finex-postgres      Started
```

Ellenőrzés:

```powershell
docker ps
```
✅ Ha jó: van egy `finex-postgres` sor, a PORTS oszlopban `0.0.0.0:5432->5432/tcp`.

Az adatbázis üresen indul: a sémát és a demóadatokat a backend hozza létre az első indításkor (Flyway).
A konténer a Docker Desktoppal együtt magától újraindul (`restart: always`).

### 4.2 Backend – A változat: terminálból (a legegyszerűbb)

Új terminálfül, a `FineX` mappából:

```powershell
cd backend
```

```powershell
.\mvnw.cmd spring-boot:run
```

Az első futás **több percig** tart (letölti a Mavent és az összes függőséget a `%USERPROFILE%\.m2` mappába).

✅ Ha jó, a napló végén ilyesmi látszik:
```
Successfully applied 5 migrations to schema "public", now at version v4.1
Tomcat started on port 8080 (http)
Started FinexApplication in … seconds
```

Ellenőrzés a böngészőben: http://localhost:8080/swagger-ui.html – megjelenik az API-dokumentáció.
Leállítás: `Ctrl + C` a terminálban.

### 4.2 Backend – B változat: Eclipse-ből

1. Indítsd az Eclipse-t; workspace-nek választhatod a `FineX` mappát (a `.metadata` mappát a git figyelmen kívül hagyja)
   vagy egy külön mappát.
2. **File → Import… → Maven → Existing Maven Projects → Next**.
3. Root Directory: **Browse…** → `…\Desktop\FineX\backend` → a `pom.xml` be van pipálva → **Finish**.
4. Várd meg, amíg jobb alul eltűnik a folyamatjelző (Maven letöltés, első alkalommal percekig tart).
5. Jobb klikk a projekten → **Maven → Update Project…** (Alt+F5) → OK.
6. Futtatás: `src/main/java` → `hu.finex.main` → **`FinexApplication.java`** → jobb klikk → **Run As → Spring Boot App**
   (vagy Java Application).
7. A Console nézetben ugyanaz a „Started FinexApplication” sor jelenik meg, mint az A változatnál.

Git Eclipse-ből: **Window → Show View → Other… → Git → Git Staging** (itt commitolsz, ahogy eddig).

> Ha az Eclipse sok piros hibát mutat a `getX()` / `builder()` hívásoknál: a Lombok nincs telepítve (lásd 1.3), vagy
> az Eclipse-t nem indítottad újra utána.

### 4.3 Frontend

Új terminálfül, a `FineX` mappából:

```powershell
cd frontend
```

```powershell
npm install
```

✅ Ha jó: `added … packages … in …s` (első alkalommal 1–2 perc). Csak az első alkalommal kell, vagy ha a
`package.json` változik.

```powershell
npm run dev
```

✅ Ha jó:
```
VITE v8.…  ready in … ms
➜  Local:   http://localhost:5173/
```

Nyisd meg: **http://localhost:5173** → átirányít a belépő oldalra.

### 4.4 Belépés

A demó felhasználók e-mail címei és jelszavai a [`database/README.md`](../database/README.md#demó-felhasználók)
fájlban vannak (két sima felhasználó és egy admin). Új fiókot a Regisztráció oldalon is létrehozhatsz.

---

## 5. Napi indítás (ha már minden telepítve van)

1. Docker Desktop fusson (az adatbázis-konténer magától elindul vele; ha mégsem: `database` mappában `docker compose up -d`).
2. Backend: Eclipse → `FinexApplication` futtatása, vagy `backend` mappában `.\mvnw.cmd spring-boot:run`.
3. Frontend: `frontend` mappában `npm run dev` → http://localhost:5173
4. Mielőtt dolgozni kezdesz, húzd le a legfrissebbet (ha a másik gépen is dolgoztál):

```powershell
git pull
```

---

## 6. Tesztek

### Backend (431 teszt, kb. 2–4 perc)

A Docker Desktopnak futnia kell (a tesztek saját, ideiglenes PostgreSQL konténert indítanak – a fejlesztői adatbázishoz
nem nyúlnak). A `backend` mappában, **a futó backendet előtte nem kell leállítani**:

```powershell
.\mvnw.cmd test
```

✅ Ha jó: `Tests run: 431, Failures: 0, Errors: 0, Skipped: 0` és `BUILD SUCCESS`.

Eclipse-ből: jobb klikk a projekten → **Run As → JUnit Test**.

### Frontend

A `frontend` mappában:

```powershell
npm run lint
```
✅ Ha jó: nem ír ki hibát.

```powershell
npm run build
```
✅ Ha jó: `✓ built in …` (a TypeScript-ellenőrzés is lefut).

---

## 7. AI-asszisztens (Claude Code) az új gépen

1. Telepítsd a Claude asztali appot: https://claude.ai/download, és jelentkezz be.
2. A **Code** fülön válaszd ki a `FineX` mappát.
3. A gyökérben lévő `CLAUDE.md`-t automatikusan beolvassa (benne vannak a munkaszabályok). Első üzenetnek ennyi elég:
   > Olvasd el a docs/AI_HANDOFF.md-t, és folytassuk a FineX-et.

A régi gépen az AI saját memóriájában lévő minden fontos tudnivaló át van írva a `CLAUDE.md`-be és a
`docs/AI_HANDOFF.md`-be, ezért ott nem kell semmit átmásolni.

---

## 8. Hibaelhárítás

| Hiba | Ok és megoldás |
|---|---|
| `npm : File … npm.ps1 cannot be loaded because running scripts is disabled` | Lásd 2.1: `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` |
| `'node' / 'java' / 'git' is not recognized` | Telepítés után zárd be és nyisd újra a terminált / VS Code-ot (teljesen, minden ablakát). Ha még mindig: indítsd újra a gépet. |
| `error during connect … dockerDesktopLinuxEngine` | Nem fut a Docker Desktop – indítsd el, várd meg az „Engine running” állapotot. |
| Backend: `Connection to localhost:5432 refused` | Nem fut az adatbázis-konténer: `database` mappában `docker compose up -d`. |
| Backend: `password authentication failed for user "finex"` | Valószínűleg egy natív PostgreSQL foglalja az 5432-es portot. Ellenőrzés: `Get-Service postgresql*` – ha fut, a Szolgáltatások (services.msc) ablakban állítsd le és állítsd „Kézi” indításra. |
| Backend: `Port 8080 was already in use` | Már fut egy backend (pl. Eclipse-ben és terminálban is). Ki kell lőni: `netstat -ano \| findstr :8080`, majd a Feladatkezelőben a PID alapján. |
| Backend: `Validate failed: Migrations have failed validation` / checksum | Egy már lefuttatott migrációs fájl módosult. Fejlesztői gépen a legegyszerűbb az adatbázis újraépítése (minden adat elvész): `database` mappában `docker compose down -v`, majd `docker compose up -d`, és a backend újraindítása. |
| Frontend: a terminálban `http proxy error … ECONNREFUSED`, a felületen hálózati hiba | Nem fut a backend (8080). |
| Frontend: belépés után azonnal kiléptet | A backend újraindult más JWT-titokkal, vagy lejárt a token – lépj be újra. |
| Tesztek: `Could not find a valid Docker environment` | Nem fut a Docker Desktop. |
| Tesztek: Docker API verzió hiba (`client version … is too old`) | Egy nagyon új Docker Engine és a régebbi Testcontainers nem érti egymást – a `backend/pom.xml`-ben emelni kell a `testcontainers-bom` verzióját (kérd az AI-t). |
| Eclipse: piros hibák a Lombok-os kódban | Lombok telepítése (1.3), Eclipse újraindítás, majd Maven → Update Project. |
| Eclipse: `Project configuration is not up-to-date with pom.xml` | Jobb klikk → Maven → Update Project… |
| Git: `LF will be replaced by CRLF` figyelmeztetés | Ártalmatlan (`core.autocrlf=true`). |
