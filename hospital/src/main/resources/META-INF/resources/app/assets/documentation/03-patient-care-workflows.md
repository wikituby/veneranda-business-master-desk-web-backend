# Patient Care Workflows

This guide covers day-to-day clinical workflows: queue, opening a visit, consultations, procedures, and the pharmacy (Treatment) tab.

## Workflow diagram

```
Registration / return patient
        │
        ▼
   Added to QUEUE (optional)
        │
        ▼
   Open PATIENT VISIT
        │
        ├── Vitals
        ├── Consultations
        ├── Procedures (services)
        ├── Treatment (drugs)
        ├── Finance
        └── Printouts
        │
        ▼
   Discharge / refer (queue updated)
```

## Opening a patient visit from the queue

1. Header → **Queue**.
2. On the **Current** tab, locate the patient.
3. Click the patient name or the **clipboard** icon in the actions column.
4. The queue modal closes and **Patient visit** opens with that patient’s context.

The system attempts to load:

- The visit linked to the queue entry (`patientVisitId`), or
- The **initial visit** for the patient if no visit ID is present

Patient and visit data are held in the browser session for the visit screen.

## Patient visit — Patient bio

Use this tab to confirm:

- Correct patient identity (name, file number, age, sex)
- Visit date and attending context

Correct any demographic errors according to your facility policy (some fields may be read-only for certain roles).

## Initial vitals

Record baseline measurements for the encounter:

- Temperature, blood pressure (systolic/diastolic), pulse, respiratory rate, SpO₂, etc.

Save vitals before clinical documentation where your protocol requires it. Vitals may feed into printouts and consultation summaries.

## Consultations

The consultations tab holds structured clinical documentation for the encounter, including:

- General consultation results
- Specialized forms (e.g. MRDT, urinalysis, CBC, parasitology) when configured for your facility

**Good practice:**

- Save each section after entry
- Use notes and recommendation fields for follow-up instructions
- Link lab or scan orders through your facility’s standard procedure (often via Procedures or dedicated lab/scan modules)

## Procedures

The **Procedures** tab lists services and procedures attached to the visit (consultation fees, nursing procedures, etc.).

Typical steps:

1. Add or select services from the facility catalogue
2. Set quantities and pricing as allowed by your role
3. Save — charges may flow to the **Finance** tab

## Treatment (pharmacy) tab

Pharmacists and authorized clinicians prescribe medicines here.

### Stock information line

Above the drug selector, labels show:

| Label | Meaning |
|-------|---------|
| **Stock** | Total quantity across all stores (distribution total) |
| **Pharmacy** | Quantity in the pharmacy store |
| **Central store** | Quantity in central store |
| **Unit buy / sell** | Pricing context for the selected batch |

### Stock transfer icon

If **Pharmacy** stock is **0** but **Stock** (total) is greater than zero, a **transfer** icon appears. Use it to move stock from central store to pharmacy (requires appropriate permissions).

### Prescribing a drug

1. Open the **Treatment** dropdown (caret on the left; label shows *Treatment* or the selected item name).
2. Search in the dropdown list if needed.
3. Select a **stock batch** for the item.
4. Complete dosage fields: amount per dose, frequency, duration, quantity, route, instructions.
5. Click **Save** for the treatment line.
6. Use **Clear field** before prescribing another item.

### Treatment chart

Where enabled, a **treatment chart** tracks administrations over time (doses given, next dose, remaining units). Use edit/delete on chart rows according to policy.

### UI notes (drug field)

- The drug selector supports long names with ellipsis; hover for the full name.
- Field width and height are tuned for the prescribing row; dropdown list scrolls for many items.

## Finance tab (visit level)

Review and manage charges generated from procedures, pharmacy, and other services for **this visit**. Coordination with the central **Cashier** module may be required to complete payment.

## Printouts

Generate patient-facing or internal documents (prescriptions, summaries, lab forms) depending on templates configured at your facility.

## Lab, scan, and dental

These departments often use dedicated screens:

| Module | Route (examples) |
|--------|------------------|
| Laboratory | `/overview/lab` or `/lab` |
| Radiology / scan | `/overview/scan` or `/scan` |
| Dental | `/overview/dental` or `/dental` |

Worklists and mobile lab views support bench processing. Results may be reflected back on the patient visit consultation tab when integrated.

## Discharge and referral

When the encounter ends:

- Update queue status (e.g. discharged, referred) via queue edit or your discharge workflow
- **Discharged today** and **Referred today** queue tabs help reception track completed movements

## Common issues

| Problem | What to check |
|---------|----------------|
| Cannot open visit from queue | Patient ID on queue entry; network/API connection |
| Drug not in dropdown | Item not stocked, batch inactive, or search filter |
| Cannot save prescription | Required dosage fields; stock batch selection |
| Transfer icon missing | Pharmacy stock > 0, or total stock is 0 |
| Tab empty | Visit not loaded; reload from queue or patient list |

---

*Document 03 — Patient care workflows — HMS documentation v1.0*
