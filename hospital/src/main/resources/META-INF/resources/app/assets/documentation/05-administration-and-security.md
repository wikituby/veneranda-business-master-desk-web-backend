# Administration and Security

For facility administrators, medical directors (MD), and IT staff who manage users, access, and configuration.

## Administration menu (Overview)

Common paths under **Overview → Administration**:

| Module | Purpose |
|--------|---------|
| User management | Create and maintain user accounts |
| Access control | Page-level and API endpoint permissions |
| Business settings | Facility name, branding, sidebar, OTC options |
| Department setup | Departments, modules, rooms for queue routing |
| Subscription | License / expiry status |
| System configuration | Global technical options |
| Data backup | Backup operations |
| Audit logs | Review user actions |

Access to these screens is typically limited to **admin** and **MD** roles.

## User management

Tasks:

- Register staff with username, email, role, department
- Assign departments and rooms the user can work in
- Deactivate users who leave the facility

**Policy recommendations:**

- One account per person; no shared passwords
- Remove access the same day employment ends
- Use strong passwords; enforce periodic changes if supported

## Access control

**Path:** Overview → **Access control**

Two layers work together:

### 1. User access (pages)

Grants individual users access to Overview pages (modules) beyond their role default.

Use when:

- A nurse needs a one-off finance report
- A pharmacist needs stock transfer page without full admin rights

### 2. Role access (API endpoints)

Maps **roles** to sensitive API operations independent of the UI.

Example endpoints in the catalog:

| Key | Operation |
|-----|-----------|
| `stock.transfer.create` | POST stock transfer |
| `stock.transfer.list` | GET transfer history |

Users with the linked **page** (e.g. stock-transfer) may also inherit endpoint rights via page key mapping.

**Enforcement:** The backend checks JWT-authenticated requests against role endpoint rules. Unauthorized calls return forbidden responses.

## Business settings

**Path:** Overview → **Business settings**

Typical configuration:

- **Facility name** — shown in headers and documents
- **Medical records header line** — printed forms
- **Contact / inquiry line** — footer on records
- **Sidebar stacked** — docked sidebar vs overlay
- **OTC include prescribing** — full dosing fields on OTC counter

Changes are saved to the server and cached in the browser session (`facilityBranding`).

## Department setup

Defines:

- Departments and clinical modules
- Rooms used as queue destinations
- Which areas appear in the header department links

Coordinate with queue workflow training so reception sends patients to the correct **to module**.

## Subscription

The **subscription guard** blocks core clinical routes when subscription is invalid or expired.

Administrators should:

- Monitor expiry warnings in Overview
- Renew before clinical staff are locked out
- Plan read-only export if migration is needed

## Queue configuration

Queue behavior depends on:

- Active room / department selection in the header
- Module definitions from department setup
- Staff permissions to edit queue entries

Train staff on status values: **WAITING**, in progress, completed, referred, etc.

## Audit and compliance

- Review **audit logs** regularly for unusual edits (stock, billing, user changes)
- Run **data backup** on a schedule defined by your IT policy
- Restrict MD/admin accounts; use lower-privilege accounts for daily work

## Security checklist

| Control | Status |
|---------|--------|
| HTTPS in production | Recommended |
| JWT on protected APIs | Enabled for sensitive endpoints |
| Role-based endpoint map | Configure per facility policy |
| Session logout on shared PCs | User training |
| Backup tested restore | IT procedure |

## Branding per facility

The same software build can present different names (e.g. clinic name on login) via business settings and assets. Marketing landing pages may use a separate public brand; operational screens use **facility name** from settings.

---

*Document 05 — Administration and security — HMS documentation v1.0*
