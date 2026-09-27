# Expense Management

Guide for recording hospital expenses, categories, and expense accounts.

**Path:** Overview → Finance → **Expenses** (`/overview/expenses`)

## Setup order

Always configure in this order:

1. **Categories** — group expenses (e.g. Utilities, Salaries, Supplies)
2. **Accounts** — specific ledgers under each category (e.g. Electricity under Utilities)
3. **Transactions** — individual expense payments

A hint bar at the top of the Expenses page shows this order.

---

## Creating expense categories

1. Open the **Categories** tab.
2. Click **New Category**.
3. Enter **Category name** and optional **Description**.
4. Click **Save Category**.

Examples: `Operational Expenses`, `Medical Supplies`, `Transport`.

---

## Creating expense accounts (Accounts tab)

Expense accounts are the accounts you post expenses against when recording a transaction.

### Steps

1. Ensure at least one **category** exists (Categories tab).
2. Open the **Accounts** tab.
3. Read the on-page **How to create expense accounts** panel (step-by-step).
4. Click **New Account**.
5. Select a **Category**.
6. Enter **Account name** (e.g. `Office Rent`, `Diesel`, `Cleaning supplies`).
7. Optional: add a **Description**.
8. Click **Save Account**.

The new account appears in the accounts table and becomes available when recording expenses.

### Accounts tab reference

| Column | Meaning |
|--------|---------|
| ID | System identifier |
| Account | Account name |
| Category | Parent expense category |
| Description | Notes |
| Created | Date account was added |

---

## Recording an expense (Transactions tab)

1. Open the **Transactions** tab.
2. Click **New Expense**.
3. **Expense account** — use the modern dropdown (see below).
4. Enter **Amount**, **Paid to / Receiver**, and **Description**.
5. Click **Save Expense**.

### Expense account dropdown

The expense account field is a searchable dropdown:

**First row (toolbar):**

| Control | Action |
|---------|--------|
| **Search** | Filter accounts by name or category |
| **New account** | Create an account without leaving the form |

**Account list:** Click an account to select it. The chosen account shows on the closed dropdown.

### If the account you need is not listed

While **Record Expense** is open:

1. Click the expense account field to open the dropdown.
2. Optionally type in **Search** (e.g. `Fuel`) — the name can pre-fill the quick-create form.
3. Click **New account** on the right of the first row.
4. A quick form appears: choose **Category**, confirm **Account name**.
5. Click **Create & select** — the account is saved and automatically selected for this expense.
6. Complete amount and other fields, then **Save Expense**.

If **New account** is disabled or shows an error, create a **category** first (Categories tab).

### Alternative: full account modal

- **Accounts** tab → **New Account** (full form with description), or
- From **Record Expense**, use **New account** in the dropdown for a faster create.

---

## Filtering transactions

On the Transactions tab:

- **Search** — reference, account, receiver, user, description
- **Account filter** — one expense account
- **Date** — single day

---

## Deleting a transaction

1. Transactions tab → **Delete** (trash) on the row.
2. Confirm in the dialog.

Deletion is permanent.

---

## Monthly summary

The **This Month** stat card totals expenses recorded in the current calendar month.

---

## Roles and access

Access to the Expenses module depends on user role and **Access control** page permissions. Finance and administration roles typically maintain categories and accounts; departmental staff may only record transactions for allowed accounts.

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Cannot save expense | Select an expense account; enter amount &gt; 0 |
| New account needs category | Create category on Categories tab first |
| Account not in list after create | Click **Refresh** or reopen the dropdown |
| Dropdown closes when clicking | Click inside the menu; use Search / New account on the top row |

---

*Document 07 — Expense management — HMS documentation v1.0*
