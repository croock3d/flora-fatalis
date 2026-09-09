# Flora Fatalis

Prywatna web/PWA do wspólnego zarządzania roślinami (2 osoby, household). Wzorzec: projekt `dietty` (katalog obok).

## Stack

- Frontend: Angular 21, CSS, service worker, Vitest
- Backend: Java 21, Spring Boot 4, hexagonal (`domain` / `application` / `adapter`)
- DB: PostgreSQL, Liquibase, JPA, UUID, `timestamptz`
- Auth: JWT (jjwt), Spring Security
- Pakiet: `com.crooked.florafatalis`
- Katalogi: `flora-fatalis-backend/`, `flora-fatalis-frontend/`

Kopiuj konwencje z dietty. Nie dodawaj warstw, których tam nie ma. Po każdej skończonej funkcjonalności zrób commit.

## Uruchomienie

```bash
docker compose up -d --build   # http://localhost:8080
```

Dev bez Dockera: Postgres (`docker compose up -d postgres` nie wystarczy — w compose Postgres nie jest wystawiony na hosta), `./mvnw spring-boot:run` w backendzie, `nvm use 22 && npm start` we frontendzie.

Dev login: `dev@flora-fatalis.local` / `devpass`

Testy: `./mvnw test` (backend), `nvm use 22 && npx ng test --watch=false` (frontend). Format Java: `./mvnw com.spotify.fmt:fmt-maven-plugin:format`.

## Architektura — nie łamać

- Dane roślin i historii należą do **aktywnego householdu**, nie do usera.
- Pielęgnacja = `care_events` + wymienna `CarePolicy`. Nie zapisuj `next_watering_at` jako faktu.
- MVP podlewania: `IntervalCarePolicy` (gatunek ± override na roślinie). Dashboard liczy dziś/zaległe/wkrótce.
- Zdjęcia: port `PhotoStorage` (dev/prod Docker: dysk + volume). Kompresja po stronie klienta.
- Roślin nie kasuj twardo — `archived_at`.
- Strefa dashboardu: `Europe/Warsaw`.

## Gotowe w MVP

Auth, household (invite), species (katalog + „Inny”), locations, plants CRUD, zdjęcia, podlewanie, dashboard, PWA shell, Docker Compose.

## Poza MVP (nie implementuj, chyba że user prosi)

Push, nawożenie, przesadzanie, przycinanie, notatki, sensory, HA, pogoda, ilość wody w UI (`quantity_ml` może zostać puste), Cloudinary/R2.

## Frontend

Feature foldery: `auth`, `household`, `plants`, `species`, `locations`, `dashboard`, `care`, `photos`, `shared`. Wzorce: `*-api.service.ts`, `*.store.ts`, `*-page.ts`.

## Backend

Moduły: `user`, `household`, `species`, `location`, `plant`, `care`, `photo`, `shared`. Gatunków nie zmyślaj — seed w Liquibase (`009-seed-species-catalog.yml`).
