# System Overview

## Purpose

The Hospital Management System (HMS) is a web-based platform for running day-to-day operations at a health facility: registering patients, managing visits, documenting clinical care, dispensing medicines, tracking inventory, billing, laboratory and imaging workflows, and administration.

One installation can serve a single clinic or a larger hospital, depending on which modules are enabled and how user access is configured.

## High-level architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Web browser (staff workstations, tablets)                  │
│  Angular frontend — patient visit, overview modules, lab…   │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS / HTTP (JWT authentication)
┌───────────────────────────▼─────────────────────────────────┐
│  Backend API (Java / Quarkus-style health_care service)     │
│  Patients, visits, stock, billing, users, queue, lab, etc.  │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Database + file storage (reports, profile images, backup)  │
└─────────────────────────────────────────────────────────────┘
```

Staff use a **header bar** for quick actions (queue, department shortcuts) and an **Overview** area with a sidebar for full module navigation.

## Main functional areas

### 1. Patient management

- Register and search patients
- Patient groups, profiles, visit history
- Appointments and referrals
- **Queue management** — patients waiting between departments/rooms

### 2. Clinical care (OPD / visit)

- **Patient visit** screen — central workspace per encounter
- Tabs typically include: Patient bio, Initial vitals, Consultations, Procedures, Treatment (pharmacy), Finance, Printouts
- Dedicated flows for **Lab**, **Scan (radiology)**, and **Dental** where configured

### 3. Inpatient (IPD) — where enabled

- Bed management, admissions, discharges
- Nursing notes, doctor rounds, medication charts
- Procedures and transfers

### 4. Pharmacy

- Prescribing and dispensing during a patient visit
- **OTC dispensary** for walk-in sales
- Stock visibility: pharmacy, central store, and total distribution
- Stock transfer from central store when pharmacy stock is zero

### 5. Inventory & stores

- Consumable items / hospital items
- Stock receive, batches, adjustments, stock taking
- Stock transfer between stores
- Reorder levels, suppliers, manufacturers
- Expired and near-expiry reporting

### 6. Laboratory & radiology

- Test requests, sample collection, processing, results, reports
- Scan requests, radiology reports, worklists
- Mobile-friendly lab page for bench work

### 7. Finance

- Service charges, patient billing, cashier
- Insurance billing, refunds, expenses
- Revenue and departmental reports

### 8. Human resources — where enabled

- Employee registration, departments, attendance, leave, payroll

### 9. Administration

- User management
- **Access control** — per-user pages and per-role API endpoints
- Business settings (facility name, branding, sidebar layout, OTC options)
- Department setup, subscription, backup, audit logs

### 10. Extended modules (roadmap / optional)

The Overview menu also includes sections for telemedicine, insurance claims, theatre, dialysis, blood bank, mortuary, BI, and more. Availability depends on your facility’s subscription and implementation phase.

## Key concepts

| Term | Meaning |
|------|---------|
| **Visit** | A single patient encounter (OPD or related) with vitals, notes, orders, and billing |
| **Queue entry** | Patient placed in line to move from one module/room to another |
| **Stock batch** | A received lot of an item with cost, sell price, and profit margins |
| **Module / room** | Department destination in the queue (e.g. consultation, pharmacy, lab) |
| **Role** | User type (e.g. admin, MD, clinical, pharmacy) controlling menus and API access |

## Who uses what

| Role (examples) | Typical screens |
|-----------------|-----------------|
| Reception / records | Patient registration, queue, visit list |
| Nurse | Vitals, patient visit, queue |
| Doctor / clinician | Consultations, procedures, investigations |
| Pharmacist | Patient visit → Treatment tab, OTC dispensary, stock |
| Store keeper | Stock receive, transfer, stock taking |
| Cashier | Finance tab on visit, cashier module |
| Administrator | User management, access control, business settings |
| Medical director (MD) | Full administration, dashboards |

## Data and security (summary)

- Users sign in with username and password; the session stores role and access rights.
- Sensitive API operations can require **role-based endpoint** permission (e.g. creating stock transfers).
- Facility branding and settings are loaded from the server and cached for the session.
- Regular backup and audit review are recommended (see **05-administration-and-security.md**).

---

*Document 01 — System overview — HMS documentation v1.0*
