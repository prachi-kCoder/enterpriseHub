# EnterpriseHub

Enterprise operations and service management demo built with Spring Boot 3, Java 17, MySQL, Spring Data JPA, JDBC reporting, and a responsive React frontend.

## Run locally

**Do not open `frontend/index.html` by double-clicking it.** The browser blocks Vite/React JavaScript modules on `file://` pages. Run the web server and open the HTTP address instead.

On Windows, double-click `start-enterprisehub.bat`. It starts the API in a second console and the React UI in the current console. The first run installs frontend dependencies. Keep both windows open and use `http://localhost:5173`. This launcher uses the file-backed `local` SQL profile, which saves its development database under `backend/data` and does not need Docker. For MySQL mode, use the standard setup below and set credentials matching your MySQL installation.

1. Requirements: Java 17+, Maven 3.9+, and Node.js 20+. Docker is needed only for the MySQL container route.
2. Start MySQL: `docker compose up -d db` (or use your local MySQL credentials).
3. In a second terminal, start the API from `backend`: `mvn spring-boot:run`
4. Start the UI from `frontend`: `npm install` then `npm run dev`
5. Open http://localhost:5173. API docs: http://localhost:8080/swagger-ui.html

The normal Spring profile uses MySQL; the `local` profile is a development convenience for machines without Docker/MySQL credentials. Demo API accounts use `Password123!` in either profile. In local mode, new requests and audit history survive restarts in `backend/data/enterprisehub-local`.

The API seeds demo users and requests on first start. Login with `admin@enterprisehub.demo` / `Password123!`, `manager@enterprisehub.demo` / `Password123!`, or `employee@enterprisehub.demo` / `Password123!`. Demo authentication uses JWT; change `JWT_SECRET` for any non-demo deployment. Set `SPRING_PROFILES_ACTIVE=prod` and configure `DB_URL`, `DB_USER`, `DB_PASSWORD`, and `JWT_SECRET` for a real database. Schema migrations live in `backend/src/main/resources/db/migration`.

## What’s included

- Employee, manager, and administrator roles with JWT login.
- Ticket creation, assignment, status tracking, manager approval, comments, search, filters, and pagination.
- Department/resource administration and request audit history.
- MySQL normalized schema, indexed lookups, JPA repositories, transactional workflows, and a JDBC analytics query.
- OpenAPI/Swagger, validation, centralized API errors, and mock email notifications logged by the application.
- Analytics dashboard for volume, overdue work, workload, and resolution time.
- Automatic impact/urgency priority matrix (P1–P4), four SLA targets, 20% at-risk threshold, late/on-time resolution tracking, manager/admin priority override reasons, and actor/value/reason audit history.

MongoDB event storage is intentionally an optional extension; the normalized operational source of truth is MySQL.

### Smart SLA API

- `GET /api/requests?q=&status=&priority=&page=0&size=10` — searchable, filterable pagination (employees see their own requests; managers/admins see the team queue).
- `POST /api/requests` — create with `title`, `description`, `category`, `impact`, `urgency`, and `departmentId`; the backend assigns priority and SLA deadline.
- `POST /api/requests/{id}/approve`, `/reject`, `/assign`, `/resolve` — workflow actions. Assignment accepts `{ "assigneeEmail": "..." }`.
- `POST /api/requests/{id}/priority` — manager/admin only; accepts impact, urgency, and a required reason.
- `GET /api/requests/{id}/audit` — request change history.
- `GET /api/analytics/dashboard` — JDBC KPIs and status totals (manager/admin only).

Priority mapping is deterministic: critical/high cross-impact escalations become P1; high/medium escalations become P2; medium impact or urgency becomes P3; low/low is P4. SLA targets are P1 4h, P2 8h, P3 24h, P4 72h. `AT_RISK` begins when 20% or less remains, `BREACHED` begins after the deadline, and resolved work is `COMPLETED` with recorded seconds and an on-time flag.
