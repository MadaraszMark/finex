# FineX – útmutató AI-asszisztensnek

Ezt a fájlt a Claude Code minden munkamenet elején automatikusan beolvassa. A részletes átadás (állapot, architektúra,
buktatók, előzmények): [`docs/AI_HANDOFF.md`](docs/AI_HANDOFF.md) – **új gépen / új munkamenetben először azt olvasd el.**
Telepítés és indítás: [`docs/TELEPITES.md`](docs/TELEPITES.md).

## A projekt egy mondatban

A FineX egy demó online bank: a felhasználó (Madarász Márk) **BSc szakdolgozata** – Spring Boot REST API (`backend/`),
React webes felület (`frontend/`), PostgreSQL Dockerben (`database/`). Publikus repó: https://github.com/MadaraszMark/finex

## Kommunikáció és munkamód (a felhasználó kérései – kötelező)

1. **Magyarul** kommunikálj. A kódban a kommentek és a felületi szövegek is magyarok; a commit-üzenetek angolok.
2. **A backend kódstílusa marad, ahogy a felhasználó írta** („a kódstílus ez marad, ahogy én csináltam eddig is!”).
   Új backend kód pontosan a meglévő minták szerint készül (lásd AI_HANDOFF.md → Backend kódstílus).
3. **A parancsokat a felhasználó futtatja** (indítás, telepítés, git): „ne te, majd én”. Adj számozott, részletes
   lépéseket (hova kattintson, mit írjon be, mi a várt kimenet: „✅ Ha jó: …”, és lépésenként hibaelhárítást).
   PowerShell-kompatibilis parancsok, **egy parancs egy kódblokkban**, `&&` nélkül. Utána csak olvasva ellenőrizz.
   Fájlokat (kód, konfiguráció) viszont te írsz.
4. **Ellenőrzés: csak könnyű statikus ellenőrzés** – frontendnél `tsc -b`, `eslint .`, `vite build`. **Ne** indíts
   Dockert, konténert, backendet, dev szervert vagy böngészős tesztet („Ennyire durva ellenőrzéseket ne futass!”),
   kivéve ha a felhasználó kifejezetten részletes tesztet kér – akkor utána mindent, amit elindítottál, állíts le.
   Maven-teszt csak akkor, ha backend kód változott (a Testcontainers miatt Docker kell hozzá).
5. **Git**: ágak `main` és **`main-mmark-dev`**; minden munka a `main-mmark-dev`-en. Alapból **ne commitolj és ne
   pusholj** – a felhasználó Eclipse-ben (Git Staging) commitol. Csak kifejezett kérésre: logikai Conventional Commits
   (angol tárgysor ≤ 72 karakter), push a dev ágra, majd a `main` fast-forward (`git push origin main-mmark-dev:main`,
   utána `git fetch origin main:main`). Ne javasolj feature branch + PR folyamatot. A git indexhez (`git add/rm`)
   kérés nélkül ne nyúlj.
6. **Belépési adatokat ne ismételj a chatben** – a demó felhasználók a `database/README.md`-ben vannak. A felhasználó
   valódi jelszavát soha ne írd be sehova.
7. `database/dumps/finex-2026-01-14.backup` – **kérdezés nélkül ne töröld** (nyitott adatvédelmi kérdés, lásd AI_HANDOFF.md).

## Design-elvárások (frontend)

- Lila **aurora / üveg (glass)** stílus, animált, modern; nagy (≈2000 px) monitoron is jól kell kinéznie, 320 px-ig.
  Világos és sötét mód. Minden oldal `PageHeader`-rel kezdődik, a kártyák `Card` / `cardSurface`.
- **Szimmetrikus kártyarácsok**: oldalanként egy oszloprendszer, egységes köz (`lg:gap-6`), egy sorban egyforma magas
  kártyák (`flex flex-col`, gombok `mt-auto`-val lent), oldalsó kártyáknál soha `items-start`. Minden reszponzív rács
  `grid-cols-1` alappal indul.
- **Nincs dummy adat** – még dekorációban sem; minden az adatbázisból jön. Az üzleti szabályok (napi limit, max.
  számlaszám stb.) a `GET /settings` végpontról (`useBankSettings`), nem frontend-konstansból.
- **Betöltéskor nem pörgő karika, hanem az oldal elrendezését követő skeleton** (`components/skeletons/SkeletonKit.tsx`
  + oldalanként `…Skeleton`, a route `hydrateFallbackElement`-je is). Spinner csak gombban / kapcsolóban.

## Gyors parancsok

| Mit | Hol | Parancs |
|---|---|---|
| Adatbázis indítása | `database/` | `docker compose up -d` |
| Backend indítása | `backend/` | `.\mvnw.cmd spring-boot:run` (vagy Eclipse: `FinexApplication`) |
| Backend tesztek (431 db, Docker kell) | `backend/` | `.\mvnw.cmd test` |
| Frontend telepítés / indítás | `frontend/` | `npm install`, majd `npm run dev` |
| Frontend ellenőrzés | `frontend/` | `npm run lint`, `npm run build` |

Portok: PostgreSQL 5432, backend 8080 (Swagger: `/swagger-ui.html`), frontend 5173 (a Vite a `/api/...` kéréseket a
backendnek továbbítja, az `/api` előtag nélkül).
