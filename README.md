# Electricity Billing System

A full-stack academic project implementing an Electricity Billing System, built strictly from
the attached Requirements Specification & Sprint Backlog (user stories US001-US019).

- **Backend**: Java 21, Spring Boot 3, Spring Data JPA / Hibernate, Spring Security (session-based,
  no JWT), Bean Validation, SQLite, Maven, JUnit 5 + Mockito.
- **Frontend**: Angular 18 (standalone components), Angular Material, TypeScript, Reactive Forms, SCSS.

## Project Structure

```
Electricity/
  backend/     Spring Boot REST API (SQLite database)
  frontend/    Angular single-page application
```

Backend package layout (`com.electricity.billing`): `controller`, `service`, `serviceimpl`,
`repository`, `entity`, `dto`, `exception`, `config`, `util`, `validation`.

Frontend layout (`src/app`): `core` (models/services/guards/interceptors), `shared`
(layouts, reusable components, validators), `features` (`auth`, `customer`, `admin`, `sme`).

## Running the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. A SQLite database file is created at
`backend/data/ebs.db` on first run, and sample data (admin, SME and two demo customers with
bills/complaints) is seeded automatically the first time the `users` table is empty.

Sample logins seeded by `DataInitializer`:

| Role     | User ID       | Password      |
|----------|---------------|---------------|
| Admin    | `admin1`      | `Admin@123`   |
| SME      | `sme1`        | `Sme@1234`    |
| Customer | `john.doe`    | `Welcome@1234`|
| Customer | `acme.traders`| `Welcome@1234`|

Run the backend test suite (JUnit 5 + Mockito, 45 tests covering registration, login, customer
bill/complaint CRUD, validation and exception handling):

```bash
cd backend
mvn test
```

## Running the frontend

```bash
cd frontend
npm install
npm start
```

`npm start` runs `ng serve --proxy-config proxy.conf.json`, which proxies all `/api/**` calls to
the backend at `http://localhost:8080` so the app runs same-origin in the browser (no CORS/CSRF
cookie issues). Open `http://localhost:4200`.

To build a production bundle: `npm run build` (output in `frontend/dist/frontend`).

## Authentication model

- Session-based login (`HttpSession` + Spring Security `SecurityContext`), **not** JWT.
- Passwords are BCrypt-encoded before being persisted.
- CSRF protection uses a cookie-based token (`XSRF-TOKEN` / `X-XSRF-TOKEN`), which Angular's
  `HttpClient` attaches automatically to state-changing requests.
- Roles: `CUSTOMER`, `ADMIN`, `SME`, enforced with method-level `@PreAuthorize` on the service
  layer (controllers stay thin and only translate HTTP <-> service calls).
- Admin-created customers get a random default password and must change it on first login
  (`mustChangePassword` flag).

## Feature coverage (SRS user stories)

**Customer**: self-registration (US001), dashboard/home (US002), view & select bills (US003),
bill summary (US004), pay bill by card (US005), invoice generation (US006), bill history with
filters/sorting/export (US007), register complaint (US008), track complaint status (US009),
complaint history (US010).

**Admin**: add customer (US011), list consumers with filters/pagination (US012), update customer
(US013), disconnect/reconnect consumer (US014), add bill with duplicate/date validation (US015),
bulk bill upload via CSV, view bill history with export (US016), search/view complaints (US017),
update complaint status with remarks and SME assignment (US018).

**SME**: search/view assigned complaints and update status with remarks (US019).

## Notes on scope

This is an academic project; a few pragmatic simplifications were made intentionally:

- Card payments are simulated (no external payment gateway) - only the last 4 digits of the
  card are stored, never the full PAN or CVV.
- PDF invoices/receipts/exports are generated server-side with Apache PDFBox; tabular exports
  are also available as CSV.
- SQLite is used per the spec; Hibernate's community SQLite dialect handles DDL generation.
