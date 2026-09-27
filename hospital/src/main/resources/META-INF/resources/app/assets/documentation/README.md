# Hospital Management System — Documentation

Welcome to the documentation set for the **Hospital Management System (HMS)** web application. These guides are written for facility staff, administrators, and technical support.

The application is **multi-tenant**: each hospital or clinic can configure its own name, branding, and business rules under **Administration → Business settings**.

## Document index

| Document | Audience | Description |
|----------|----------|-------------|
| [01-system-overview.md](./01-system-overview.md) | All users | What the system does, main modules, and how areas connect |
| [02-getting-started.md](./02-getting-started.md) | New users | Login, navigation, roles, and daily basics |
| [03-patient-care-workflows.md](./03-patient-care-workflows.md) | Clinical staff | Queue, patient visit, consultations, procedures, pharmacy tab |
| [04-pharmacy-and-inventory.md](./04-pharmacy-and-inventory.md) | Pharmacy & stores | Stock receive, batches, transfers, OTC, dispensing |
| [05-administration-and-security.md](./05-administration-and-security.md) | Admins / MD | Users, access control, business settings, subscriptions |
| [06-technical-guide.md](./06-technical-guide.md) | IT / developers | Architecture, deployment, API connection, support |
| [07-expense-management.md](./07-expense-management.md) | Finance staff | Categories, expense accounts, recording expenses, dropdown |

## How to use these files

- **In the application:** Overview → **Administration & Setup** → **System Documentation** (or account menu → Documentation).
- **PDF or print:** Open any `.md` file in VS Code, Cursor, or a Markdown viewer and export to PDF, or paste into Word/Google Docs.
- **Share with clients:** Copy the whole `documentation` folder; paths are relative and work offline.
- **Updates:** When features change, update the relevant numbered guide and note the date at the bottom of that file.

## Version information

| Item | Value |
|------|--------|
| Frontend | Angular 16 (project: `wiki` / ngwiki) |
| Backend API context | `/health_care` (default port 8080) |
| Documentation pack | 1.0 — June 2026 |

For questions about a specific screen, start with **02-getting-started**, then the module guide that matches your role.
