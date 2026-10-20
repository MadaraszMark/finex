# FineX – frontend

A FineX webes felülete: React + TypeScript, Vite, Tailwind CSS.

## Indítás

Előtte fusson az adatbázis (Docker) és a backend (Eclipse, 8080-as port). Ebben a mappában:

```bash
npm install
```

Ez csak az első alkalommal kell, vagy ha új csomag kerül a projektbe. Utána:

```bash
npm run dev
```

Majd a böngészőben: http://localhost:5173

## Hogyan éri el a backendet?

A frontend minden kérést `/api/...` címre küld. A Vite dev szerver ezeket továbbítja a `http://localhost:8080` címre, az `/api` előtag nélkül (`vite.config.ts` → `server.proxy`). A böngésző így csak az 5173-as porttal beszél, ezért nincs CORS-hiba.

Bejelentkezéskor a backend JWT tokent ad. Ezt a frontend eltárolja, és minden kéréshez hozzáteszi (`src/api/client.ts`). Ha a token lejár (401), a felhasználó a belépő oldalra kerül.

## Parancsok

| Parancs | Mit csinál |
|---|---|
| `npm run dev` | Fejlesztői szerver, mentéskor azonnal frissül |
| `npm run build` | TypeScript-ellenőrzés és éles build a `dist/` mappába |
| `npm run lint` | ESLint-ellenőrzés |

## Mappaszerkezet

| Mappa | Tartalom |
|---|---|
| `src/app` | Útvonalak (oldalanként külön betöltődő kód), az alkalmazás kerete (oldalsáv, fejléc, alsó menü), Ctrl+K parancspaletta |
| `src/api` | Backend-hívások (controllerenként egy fájl), a DTO-knak megfelelő típusok, React Query kulcsok |
| `src/features` | Oldalak funkciónként: `pages/`, `components/` (az oldal betöltés közbeni vázával), `queries.ts`, `schemas.ts` |
| `src/components` | Újrahasználható felületelemek (`ui/`), márkaelemek (`brand/`), vázak (`skeletons/`), grafikonelemek (`charts/`) |
| `src/session` | Token tárolása, lejárat figyelése, útvonalvédők (bejelentkezett / vendég / admin) |
| `src/theme` | Világos / sötét / rendszer téma |
| `src/hooks`, `src/lib` | Saját React hookok, segédfüggvények (formázás, dátumok, IBAN, animációk) |
