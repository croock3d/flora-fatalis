# Flora Fatalis

Prywatna web/PWA do wspólnego zarządzania roślinami (2 osoby, household). Wzorzec: projekt `dietty` (katalog obok).

**Loop CORE:** zaloguj się → (ew. partner) → lokalizacje i rośliny → dashboard „Dzisiaj” → podlej → historia.

## Stack

- Frontend: Angular 21, CSS, service worker, Vitest
- Backend: Java 21, Spring Boot 4, hexagonal (`domain` / `application` / `adapter`)
- DB: PostgreSQL, Liquibase, JPA, UUID, `timestamptz`
- Auth: JWT (jjwt), Spring Security
- Pakiet: `com.crooked.florafatalis`
- Katalogi: `flora-fatalis-backend/`, `flora-fatalis-frontend/`

Kopiuj konwencje z dietty. Nie dodawaj warstw, których tam nie ma. Po każdej skończonej funkcjonalności zrób commit. **Nie przebudowuj Dockera** po każdej zmianie.

## Uruchomienie (dev — to jest domyślny tryb pracy)

Postgres w Dockerze, reszta lokalnie (hot reload, bez rebuildu obrazu):

```bash
docker compose up -d postgres                          # Postgres na :5433
cd flora-fatalis-backend && ./mvnw spring-boot:run    # :8080
cd flora-fatalis-frontend && nvm use 22 && npm start  # :4200, proxy /api → :8080
```

Frontend: http://localhost:4200

Pełny obraz (frontend w Springu) tylko gdy chcesz sprawdzić paczkę:

```bash
docker compose --profile packaged up -d --build   # http://localhost:8080
```

Dev login: `dev@flora-fatalis.local` / `devpass`

Testy: `./mvnw test` (backend), `nvm use 22 && npx ng test --watch=false` (frontend). Format Java: `./mvnw com.spotify.fmt:fmt-maven-plugin:format`.

## Architektura — nie łamać

- Dane roślin i historii należą do **aktywnego householdu**, nie do usera.
- Pielęgnacja = `care_events` + wymienna `CarePolicy`. Nie zapisuj `next_watering_at` jako faktu.
- Podlewanie (CORE): `IntervalCarePolicy` (gatunek ± override na roślinie). Dashboard liczy dziś/zaległe/wkrótce (7 dni).
- Nawożenie (MVP+): `FertilizingCarePolicy` (gatunek ± override). Sezon, spoczynek i typ nawozu w katalogu są **tylko informacją** — nie sterują schedulerem. Nie zmieniaj schedulera nawożenia, chyba że user prosi.
- Przycinanie (MVP+): dziennik (`PRUNING` + opcjonalnie `pruning_kind` / `notes`). **Bez** terminów i bez pozycji na dashboardzie.
- Zdjęcia (MVP+): port `PhotoStorage` (dev/prod Docker: dysk + volume). Kompresja po stronie klienta.
- Roślin nie kasuj twardo — `archived_at`. Unarchive / lista archiwum = Future.
- Strefa dashboardu: `Europe/Warsaw`.
- Household: max 2 osoby (owner + 1 partner).
- Gatunków nie zmyślaj i nie dawaj userowi CRUD — seed Liquibase (`009-seed-species-catalog.yml` plus późniejsze seed-y nawożenia `012`–`014`). Katalog read-only + „Inny”.

Future hooks w modelu (nie implementować): `CareSource.HA` / `SENSOR` / `SYSTEM`; port `PhotoStorage` pod cloud.

## CORE MVP

Auth, household 2-osobowy (własne HH + invite partnera + switch + leave), lokalizacje, gatunki (katalog + „Inny”), rośliny (lista, dodanie, edycja, szczegóły, archiwizacja jednokierunkowa), podlewanie (event + wyliczanie kolejnego terminu + override), dashboard „Dzisiaj”, historia rośliny, household scoping, timezone Europe/Warsaw, Docker Compose.

## MVP+ (jest w kodzie i UI — nie usuwać, nie rozbudowywać bez prośby)

- Nawożenie: event, terminy na dashboardzie, override na roślinie. Sezon/spoczynek/typ nawozu = tekst katalogu.
- Przycinanie: log na karcie rośliny i timeline. Bez schedulera.
- Zdjęcia: upload, galeria, primary, delete.
- Opcjonalne `quantity_ml` przy podlewaniu (nie wchodzi do wyliczania terminu).
- PWA shell (manifest + service worker assetów; to nie jest offline-first).
- Wygody household poza minimalnym 2-osobowym loopem (np. switch między własnym a partnerskim HH).

## Future (nie implementuj, chyba że user prosi)

Push / przypomnienia, Home Assistant, sensory, pogoda, adaptive watering, AI / analiza zdjęć, cloud photo storage (Cloudinary/R2), offline-first, przesadzanie, ogólne notatki (poza notatką przycinania), CRUD gatunków, unarchive / lista archiwum, zmiana hasła/profilu, >2 osoby w household, terminy przycinania.

## Frontend

Feature foldery: `auth`, `household`, `plants`, `species`, `locations`, `dashboard`, `care`, `photos`, `shared`. Wzorce: `*-api.service.ts`, `*.store.ts`, `*-page.ts`.

## Backend

Moduły: `user`, `household`, `species`, `location`, `plant`, `care`, `photo`, `shared`. `care` obsługuje `WATERING` (CORE), `FERTILIZING` i `PRUNING` (MVP+).
