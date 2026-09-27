# Pharmacy and Inventory

Guide for pharmacy staff, store keepers, and managers responsible for medicines and consumables.

## Overview

Inventory is built around **hospital items** (medicines and consumables), **stock batches** (each receipt with cost and sell price), and **store distribution** (quantities per store: pharmacy, central, others).

Pharmacy interacts with inventory through:

- **Patient visit → Treatment** (dispense on prescription)
- **OTC dispensary** (over-the-counter sales)
- **Stock transfer** (move stock between stores)
- **Overview → Inventory** modules (receive, adjust, report)

## Stores and stock labels

| Store role | Typical use |
|------------|-------------|
| Pharmacy | Dispensing to patients and OTC |
| Central store | Bulk storage; transfers to pharmacy |
| Other stores | Wards, theatre, etc. when configured |

On the patient visit Treatment tab:

- **Stock** = sum across stores
- **Pharmacy** / **Central store** = per-location quantities

## Stock receive

**Path:** Overview → Inventory → **Stock receive**

Purpose: Record goods inward and create or update **stock batches**.

Typical fields:

- Item, quantity, unit of measure
- Batch identifiers (batch number, expiry where applicable)
- Cost price, sell price
- **Profit margin for retail** (and wholesale/special case margins on batch maintenance)

Received stock increases distribution according to the target store rules.

## Stock batch management

**Path:** Overview → Inventory → **Stock batch**

Maintain batch-level economics:

| Field | Description |
|-------|-------------|
| Profit margin for retail | Margin applied to retail/standard sales |
| Profit margin for wholesale | Wholesale pricing rules |
| Profit margin for special case | Exceptions (insurance, staff, etc.) |

Legacy data may map old `profitMargin` values to retail until fully migrated.

## Stock transfer

**Path:** Overview → Inventory → **Stock transfer**

Use when stock must move between stores (e.g. central → pharmacy).

**From patient visit:** When pharmacy quantity is zero but total stock exists, use the transfer icon on the Treatment tab (requires role permission).

### Access control

Creating and listing transfers may require:

- **User access** to the Stock transfer page, and/or
- **Role endpoint access** for:
  - `stock.transfer.create` (POST transfer)
  - `stock.transfer.list` (GET transfer history)

Configure under **Overview → Access control** (User access and Role access tabs).

## OTC dispensary

**Path:** Overview → Pharmacy → **OTC dispensary**

Walk-in sales without a full patient visit workflow.

Business settings may control whether OTC includes full prescribing fields (dosage, frequency, duration) — see **Business settings → OTC include prescribing**.

## Consumable items / item catalogue

**Path:** Overview → Inventory → **Consumable items** (and related item management)

- Define items, units, categories
- Link to manufacturers and suppliers
- Set reorder levels

## Stock taking and adjustments

| Module | Purpose |
|--------|---------|
| Stock taking | Physical count vs system |
| Adjustments | Correct quantities after variance |
| Expired items | Remove or quarantine expired stock |
| Near expiry / expired drugs routes | Dedicated expiry reports |

## Reorder and suppliers

- **Reorder levels** — alerts when stock falls below threshold
- **Suppliers** — vendor master data for purchasing

## Pharmacy during a visit

1. Select drug (stock batch) on Treatment tab.
2. Enter dosing and instructions.
3. Save treatment line — stock decrements according to backend rules.
4. Reprint or verify on **Printouts** if needed.

**Clear field** resets the form for the next item without leaving the tab.

## Reporting

Useful Overview reports:

- Drug utilization
- Inventory summary
- Pharmacy expiry alerts
- Store reports / analytics

## Best practices

1. Receive stock before prescribing new batches.
2. Transfer to pharmacy before peak dispensing hours if central holds stock.
3. Review near-expiry weekly.
4. Restrict transfer and adjustment permissions to trusted roles.
5. Reconcile OTC sales with cashier totals daily.

## Troubleshooting

| Issue | Action |
|-------|--------|
| Item not selectable in visit | Check batch exists, not expired, stock > 0 in usable store |
| Transfer fails | Verify role endpoint access and JWT login |
| Wrong sell price | Update stock batch margins and sell price |
| OTC missing dosing fields | Business settings → OTC prescribing option |

---

*Document 04 — Pharmacy and inventory — HMS documentation v1.0*
