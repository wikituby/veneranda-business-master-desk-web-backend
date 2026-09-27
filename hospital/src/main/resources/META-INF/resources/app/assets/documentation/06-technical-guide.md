# Technical Guide

For IT support, implementers, and developers maintaining the HMS deployment.

## Solution stack

| Layer | Technology |
|-------|------------|
| Frontend | Angular 16, Bootstrap 5, RxJS |
| UI libraries | ng-bootstrap, Angular Material (partial), Firebase storage (profile/assets) |
| Backend | Java health care API (context path `/health_care`) |
| Auth | JWT (CustomAwareJWTAuthMechanism on backend) |
| Default API port | 8080 |

Frontend project name: `wiki` (folder: ngwiki).

## Repository layout (frontend)

```
ngwiki/
├── src/app/              # Application modules and pages
│   ├── overview/         # Main HMS modules (sidebar)
│   ├── patient-visit/    # Clinical encounter workspace
│   ├── header/           # Global header, queue
│   ├── auth/             # Login, signup, password reset
│   ├── services/         # API, access control, branding
│   └── config/           # API URLs, access endpoint catalog
├── src/assets/
│   ├── images/           # Logos, UI art
│   └── documentation/    # Client documentation (this folder)
└── angular.json          # Build configuration
```

## Running locally

```bash
npm install
ng serve
```

Browser: `http://localhost:4200/`

Ensure the backend is running and reachable at the configured base URL.

## API connection

Configuration: `src/app/config/api-server.config.ts`

| Constant | Default |
|----------|---------|
| Context path | `/health_care` |
| Port | 8080 |
| Local base | `http://localhost:8080/health_care` |

Runtime selection:

- `ApiConfigService` runs on `APP_INITIALIZER`
- Active base URL stored in `localStorage` / session keys: `apiActiveBaseUrl`, `apiSavedServers`
- Health check: `/health` with timeout (4s)

Staff may use **Server connection** UI to switch hosts on LAN deployments.

## Authentication flow

1. User posts credentials to login endpoint.
2. JWT and user metadata stored in **sessionStorage** (role, username, email, access lists).
3. `subscriptionGuard` and `blockDashboardForClinicalRoleGuard` protect routes.
4. HTTP interceptor (`GlobalFeedbackInterceptor`) surfaces API errors globally.

## Access control implementation

| Piece | Location |
|-------|----------|
| Page keys / user grants | `AccessControlService`, session cache |
| Endpoint catalog (frontend) | `src/app/config/app-access-endpoints.ts` |
| Endpoint catalog (backend) | `EndpointAccessCatalog`, `RoleEndpointAccess` entity |
| Enforcement | `EndpointAuthorizationFilter` |

When adding a new protected API:

1. Register key in backend catalog
2. Add matching entry to `APP_ACCESS_ENDPOINTS`
3. Expose toggles in Access control UI
4. Exclude path from open JWT bypass only if intentional

## Key routes

| Route | Component |
|-------|-----------|
| `/login`, `/login4` | Login variants |
| `/overview/*` | Module shell |
| `/patient-visit` | PatientVisitComponent |
| `/patient-visits-list` | Visit list |
| `/dashboard` | Dashboard (guarded) |

Full route table: `src/app/app-routing.module.ts`.

## Build for production

```bash
ng build --configuration production
```

Output: `dist/` — deploy to static web server (nginx, IIS, etc.). Configure reverse proxy to API and enable HTTPS.

## Environment

- `environment.ts` / `environment.prod.ts` — Firebase and feature flags
- Facility-specific values prefer **business settings API** over hard-coded branding

## Database and migrations

Backend uses JPA/Hibernate entities (e.g. `StockBatch` with `profitMarginForRetail`, `profitMarginForWholeSale`, `profitMarginForSpecialCase`).

After entity changes:

1. Run backend compile/deploy
2. Allow schema update or run migration scripts
3. Migrate legacy `profitMargin` column data to retail fields if applicable

## CORS and LAN deployment

Typical clinic setup:

- Frontend: one host (e.g. `192.168.x.x:4200` or port 80)
- API: `192.168.x.x:8080/health_care`
- Configure CORS on backend for frontend origin

## Backup and recovery

Use Overview → **Data backup** for application-supported backup flows. IT should also snapshot database and file storage at hypervisor or DB level.

## Support diagnostics

| Symptom | Check |
|---------|--------|
| 401 / 403 on API | JWT expiry, role endpoint map |
| Blank overview | sessionStorage role; subscription guard |
| Queue visit won't open | API `getPatientVisitByVisitId` / network tab |
| Wrong facility name | Business settings cache; clear session |

## Documentation maintenance

Update `src/assets/documentation/*.md` when:

- New modules go live for clients
- Access control or pharmacy workflow changes
- API path or port defaults change

## Version history

| Version | Date | Notes |
|---------|------|-------|
| 1.0 | June 2026 | Initial client documentation pack |

---

*Document 06 — Technical guide — HMS documentation v1.0*
