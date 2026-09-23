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
| `src/api` | Backend-hívások és a DTO-knak megfelelő típusok |
| `src/auth` | Token tárolása, be- és kijelentkezés |
| `src/components` | Újrahasználható felületelemek |
| `src/hooks` | Saját React hookok |
| `src/lib` | Segédfüggvények (formázás, lekérdezés-kliens) |
| `src/pages` | Oldalak (belépés, főoldal) |
