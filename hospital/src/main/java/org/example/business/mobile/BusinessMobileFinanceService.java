package org.example.business.mobile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.example.business.mobile.domain.MobileDebt;
import org.example.business.mobile.domain.MobileDebtPayment;
import org.example.business.mobile.domain.MobileExpense;
import org.example.business.mobile.domain.MobileExpenseAccount;
import org.example.business.mobile.domain.MobileExpenseCategory;
import org.example.business.mobile.domain.MobileLoan;
import org.example.business.mobile.domain.MobileLoanPayment;
import org.example.business.mobile.domain.MobileSale;
import org.example.business.mobile.domain.MobileSaleLine;
import org.example.business.mobile.domain.MobileServiceTxn;
import org.example.business.mobile.domain.MobileStockReceipt;
import org.example.business.mobile.domain.MobileStockTransfer;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.domains.repositories.StockItemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists Flutter mother-app sales / expenses / stock / finance rows for frontend-only mode.
 */
@ApplicationScoped
public class BusinessMobileFinanceService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    BusinessMobileMapper mapper;

    // -------------------- Sales --------------------

    @Transactional
    public Map<String, Object> createSale(Map<String, Object> body) {
        if (body == null) {
            throw new WebApplicationException("Invalid sale payload", 400);
        }
        BigDecimal totalAmount = bd(body.get("totalAmount") != null ? body.get("totalAmount") : body.get("total_amount"));
        Object itemsRaw = body.get("items");
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0 || !(itemsRaw instanceof List<?> items) || items.isEmpty()) {
            throw new WebApplicationException("Invalid sale payload", 400);
        }

        List<ParsedLine> lines = new ArrayList<>();
        for (Object row : items) {
            if (!(row instanceof Map<?, ?> m)) continue;
            Long itemId = asLong(m.get("itemId") != null ? m.get("itemId") : m.get("item_id"));
            BigDecimal qty = bd(m.get("quantity"));
            BigDecimal unitPrice = bd(m.get("unitPrice") != null ? m.get("unitPrice") : m.get("unit_price"));
            BigDecimal discount = bd(m.get("productDiscount") != null ? m.get("productDiscount") : m.get("product_discount"));
            if (discount == null) discount = BigDecimal.ZERO;
            if (itemId == null || qty == null || unitPrice == null
                    || qty.compareTo(BigDecimal.ZERO) <= 0 || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new WebApplicationException("Invalid sale item payload", 400);
            }
            StockItem item = stockItemRepository.findById(itemId);
            if (item == null) {
                throw new WebApplicationException("Item not found: " + itemId, 404);
            }
            BigDecimal onHand = mapper.resolvedStockQty(item);
            boolean serviceLike = item.saleCategory != null
                    && item.saleCategory.toLowerCase().contains("service");
            if (!serviceLike && onHand.compareTo(qty) < 0) {
                throw new WebApplicationException(
                        "Insufficient stock for " + nz(item.stockItemName) + " (have " + onHand + ", need " + qty + ")",
                        400);
            }
            BigDecimal lineTotal = unitPrice.multiply(qty).subtract(discount);
            if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;
            lines.add(new ParsedLine(item, qty, unitPrice, discount, lineTotal));
        }
        if (lines.isEmpty()) {
            throw new WebApplicationException("No sale items provided", 400);
        }

        BigDecimal amountReceived = bd(body.get("amountReceived") != null ? body.get("amountReceived") : body.get("amount_received"));
        if (amountReceived == null) amountReceived = totalAmount;
        BigDecimal overallDiscount = bd(body.get("overallDiscount") != null ? body.get("overallDiscount") : body.get("overall_discount"));
        if (overallDiscount == null) overallDiscount = BigDecimal.ZERO;
        BigDecimal balance = bd(body.get("balance"));
        if (balance == null) balance = totalAmount.subtract(amountReceived);
        if (balance.compareTo(BigDecimal.ZERO) < 0) balance = BigDecimal.ZERO;

        String paymentMethod = str(body.get("paymentMethod") != null ? body.get("paymentMethod") : body.get("payment_method"));
        if (paymentMethod.isBlank()) paymentMethod = "cash";

        MobileSale sale = new MobileSale();
        sale.storeId = asLong(body.get("storeId") != null ? body.get("storeId") : body.get("store_id"));
        sale.totalAmount = totalAmount;
        sale.overallDiscount = overallDiscount;
        sale.amountReceived = amountReceived;
        sale.balance = balance;
        sale.customerName = blankToNull(str(body.get("customerName") != null ? body.get("customerName") : body.get("customer_name")));
        sale.customerPhone = blankToNull(str(body.get("customerPhone") != null ? body.get("customerPhone") : body.get("customer_phone")));
        sale.customerAddress = blankToNull(str(body.get("customerAddress") != null ? body.get("customerAddress") : body.get("customer_address")));
        sale.paymentMethod = paymentMethod;
        sale.createdAt = LocalDateTime.now();
        sale.persist();

        for (ParsedLine line : lines) {
            boolean serviceLike = line.item.saleCategory != null
                    && line.item.saleCategory.toLowerCase().contains("service");
            if (!serviceLike) {
                deductStock(line.item, line.qty);
            }
            MobileSaleLine sl = new MobileSaleLine();
            sl.saleId = sale.id;
            sl.itemId = line.item.id;
            sl.itemName = nz(line.item.stockItemName);
            sl.itemUnit = line.item.unit;
            sl.itemCategory = line.item.businessCategory != null ? line.item.businessCategory : line.item.itemCategoryName;
            sl.quantity = line.qty;
            sl.unitPrice = line.unitPrice;
            sl.productDiscount = line.discount;
            sl.lineTotal = line.lineTotal;
            sl.persist();
        }

        if (balance.compareTo(BigDecimal.ZERO) > 0
                && sale.customerName != null && !sale.customerName.isBlank()) {
            MobileDebt debt = new MobileDebt();
            debt.storeId = sale.storeId;
            debt.saleId = sale.id;
            debt.customerName = sale.customerName;
            debt.phone = sale.customerPhone;
            debt.address = sale.customerAddress;
            debt.amount = balance;
            debt.paid = false;
            debt.createdAt = sale.createdAt;
            debt.persist();
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Sale saved");
        out.put("id", sale.id.intValue());
        out.put("saleId", sale.id.intValue());
        return out;
    }

    @Transactional
    public List<Map<String, Object>> salesHistory(String startIso, String endIso) {
        List<MobileSale> sales = MobileSale.listAll();
        sales.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        LocalDateTime start = parseDt(startIso);
        LocalDateTime end = parseDt(endIso);
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileSale sale : sales) {
            if (start != null && sale.createdAt.isBefore(start)) continue;
            if (end != null && sale.createdAt.isAfter(end)) continue;
            List<MobileSaleLine> lines = MobileSaleLine.list("saleId", sale.id);
            for (MobileSaleLine line : lines) {
                out.add(saleLineRow(sale, line));
            }
        }
        return out;
    }

    @Transactional
    public List<Map<String, Object>> salesByCustomer(String name) {
        String needle = name == null ? "" : name.trim().toLowerCase();
        List<Map<String, Object>> all = salesHistory(null, null);
        if (needle.isEmpty()) return all;
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : all) {
            String cn = str(row.get("customer_name")).toLowerCase();
            if (cn.contains(needle)) out.add(row);
        }
        return out;
    }

    @Transactional
    public boolean deleteSale(Object idObj) {
        Long id = asLong(idObj);
        if (id == null) return false;
        MobileSale sale = MobileSale.findById(id);
        if (sale == null) return false;
        List<MobileSaleLine> lines = MobileSaleLine.list("saleId", id);
        for (MobileSaleLine line : lines) {
            StockItem item = stockItemRepository.findById(line.itemId);
            if (item != null) {
                restoreStock(item, line.quantity == null ? BigDecimal.ZERO : line.quantity);
            }
            line.delete();
        }
        List<MobileDebt> debts = MobileDebt.list("saleId", id);
        for (MobileDebt d : debts) {
            d.delete();
        }
        sale.delete();
        return true;
    }

    @Transactional
    public void reverseInitialPayment(Object saleIdObj) {
        Long saleId = asLong(saleIdObj);
        if (saleId == null) throw new WebApplicationException("saleId is required", 400);
        MobileSale sale = MobileSale.findById(saleId);
        if (sale == null) throw new WebApplicationException("Sale not found", 404);
        BigDecimal received = sale.amountReceived == null ? BigDecimal.ZERO : sale.amountReceived;
        if (received.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WebApplicationException("No initial payment to reverse", 400);
        }
        sale.balance = (sale.balance == null ? BigDecimal.ZERO : sale.balance).add(received);
        sale.amountReceived = BigDecimal.ZERO;
        if (sale.customerName != null && !sale.customerName.isBlank()
                && sale.balance.compareTo(BigDecimal.ZERO) > 0) {
            MobileDebt existing = MobileDebt.find("saleId", saleId).firstResult();
            if (existing == null) {
                MobileDebt debt = new MobileDebt();
                debt.storeId = sale.storeId;
                debt.saleId = sale.id;
                debt.customerName = sale.customerName;
                debt.phone = sale.customerPhone;
                debt.address = sale.customerAddress;
                debt.amount = sale.balance;
                debt.paid = false;
                debt.createdAt = LocalDateTime.now();
                debt.persist();
            } else {
                existing.amount = sale.balance;
                existing.paid = false;
            }
        }
    }

    private Map<String, Object> saleLineRow(MobileSale sale, MobileSaleLine line) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sale_id", sale.id.intValue());
        m.put("item_id", line.itemId == null ? null : line.itemId.intValue());
        m.put("total_amount", d(sale.totalAmount));
        m.put("overall_discount", d(sale.overallDiscount));
        m.put("amount_received", d(sale.amountReceived));
        m.put("balance", d(sale.balance));
        m.put("customer_name", sale.customerName);
        m.put("customer_phone", sale.customerPhone);
        m.put("customer_address", sale.customerAddress);
        m.put("payment_method", sale.paymentMethod);
        m.put("created_at", sale.createdAt.format(ISO));
        m.put("quantity", d(line.quantity));
        m.put("unit_price", d(line.unitPrice));
        m.put("product_discount", d(line.productDiscount));
        m.put("line_total", d(line.lineTotal));
        m.put("item_name", line.itemName);
        m.put("item_unit", line.itemUnit);
        m.put("item_category", line.itemCategory);
        return m;
    }

    // -------------------- Expenses --------------------

    @Transactional
    public List<Map<String, Object>> allExpenses() {
        List<MobileExpense> rows = MobileExpense.listAll();
        rows.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileExpense e : rows) out.add(expenseToMap(e));
        return out;
    }

    @Transactional
    public Map<String, Object> saveExpense(Map<String, Object> body) {
        BigDecimal amount = bd(body.get("amount") != null ? body.get("amount") : body.get("amountTransacted"));
        String receiver = str(body.get("receiver") != null ? body.get("receiver")
                : (body.get("receivedBy") != null ? body.get("receivedBy") : body.get("received_by")));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || receiver.isBlank()) {
            throw new WebApplicationException("Invalid expense payload", 400);
        }
        Long id = asLong(body.get("id"));
        MobileExpense e = id == null ? new MobileExpense() : MobileExpense.findById(id);
        if (e == null) e = new MobileExpense();
        e.storeId = asLong(body.get("storeId") != null ? body.get("storeId") : body.get("store_id"));
        e.amount = amount;
        e.description = blankToNull(str(body.get("description") != null ? body.get("description") : body.get("notes")));
        e.receiver = receiver;
        e.expenseAccountId = asLong(body.get("expenseAccountId") != null ? body.get("expenseAccountId") : body.get("expense_account_id"));
        e.expenseAccountName = blankToNull(str(body.get("expenseAccountName") != null ? body.get("expenseAccountName") : body.get("expense_account_name")));
        e.referenceNumber = blankToNull(str(body.get("referenceNumber") != null ? body.get("referenceNumber") : body.get("reference_number")));
        e.userName = blankToNull(str(body.get("userName") != null ? body.get("userName") : body.get("user_name")));
        LocalDateTime created = parseDt(str(body.get("createdAt") != null ? body.get("createdAt") : body.get("created_at")));
        e.createdAt = created == null ? LocalDateTime.now() : created;
        if (e.id == null) e.persist();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Expense saved");
        out.put("id", e.id.intValue());
        out.put("data", expenseToMap(e));
        return out;
    }

    @Transactional
    public boolean deleteExpense(Object idObj) {
        Long id = asLong(idObj);
        if (id == null) return false;
        return MobileExpense.deleteById(id);
    }

    private Map<String, Object> expenseToMap(MobileExpense e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.id.intValue());
        m.put("store_id", e.storeId == null ? null : e.storeId.intValue());
        m.put("amount", d(e.amount));
        m.put("description", e.description);
        m.put("receiver", e.receiver);
        m.put("expense_account_id", e.expenseAccountId == null ? null : e.expenseAccountId.intValue());
        m.put("expense_account_name", e.expenseAccountName);
        m.put("reference_number", e.referenceNumber);
        m.put("user_name", e.userName);
        m.put("created_at", e.createdAt.format(ISO));
        return m;
    }

    @Transactional
    public List<Map<String, Object>> expenseCategories() {
        List<MobileExpenseCategory> rows = MobileExpenseCategory.listAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileExpenseCategory c : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.id.intValue());
            m.put("name", c.name);
            m.put("description", c.description);
            m.put("created_at", c.createdAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> saveExpenseCategory(Map<String, Object> body) {
        String name = str(body.get("name"));
        if (name.isBlank()) throw new WebApplicationException("Category name is required", 400);
        MobileExpenseCategory c = new MobileExpenseCategory();
        c.name = name;
        c.description = blankToNull(str(body.get("description")));
        c.createdAt = LocalDateTime.now();
        c.persist();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Category saved");
        out.put("id", c.id.intValue());
        return out;
    }

    @Transactional
    public List<Map<String, Object>> expenseAccounts() {
        List<MobileExpenseAccount> rows = MobileExpenseAccount.listAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileExpenseAccount a : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.id.intValue());
            m.put("account_name", a.accountName);
            m.put("description", a.description);
            m.put("category_id", a.categoryId == null ? null : a.categoryId.intValue());
            m.put("category_name", a.categoryName);
            m.put("created_at", a.createdAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> saveExpenseAccount(Map<String, Object> body) {
        String accountName = str(body.get("accountName") != null ? body.get("accountName") : body.get("account_name"));
        if (accountName.isBlank()) throw new WebApplicationException("Account name is required", 400);
        MobileExpenseAccount a = new MobileExpenseAccount();
        a.accountName = accountName;
        a.description = blankToNull(str(body.get("description")));
        a.categoryId = asLong(body.get("categoryId") != null ? body.get("categoryId") : body.get("category_id"));
        a.categoryName = blankToNull(str(body.get("categoryName") != null ? body.get("categoryName") : body.get("category_name")));
        a.createdAt = LocalDateTime.now();
        a.persist();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Account saved");
        out.put("id", a.id.intValue());
        return out;
    }

    // -------------------- Stock --------------------

    @Transactional
    public Map<String, Object> receiveStock(Map<String, Object> body) {
        Long id = asLong(body.get("itemId") != null ? body.get("itemId") : body.get("id"));
        if (id == null) return null;
        StockItem item = stockItemRepository.findById(id);
        if (item == null) return null;
        BigDecimal qty = bd(body.get("qty") != null ? body.get("qty") : body.get("quantity"));
        if (qty == null) qty = BigDecimal.ZERO;
        BigDecimal current = item.stockQty == null ? BigDecimal.ZERO : item.stockQty;
        item.stockQty = current.add(qty);

        BigDecimal unitCost = bd(first(body, "unitCost", "unit_cost", "costPrice", "cost_price"));
        if (unitCost != null) item.costPrice = unitCost;
        BigDecimal selling = bd(first(body, "sellingPrice", "selling_price"));
        if (selling != null) item.sellingPrice = selling;

        MobileStockReceipt receipt = new MobileStockReceipt();
        receipt.itemId = item.id;
        receipt.itemName = nz(item.stockItemName);
        receipt.storeId = asLong(first(body, "storeId", "store_id"));
        receipt.quantity = qty;
        receipt.unitCost = unitCost == null ? BigDecimal.ZERO : unitCost;
        receipt.totalCost = bd(first(body, "totalCost", "total_cost"));
        if (receipt.totalCost == null) {
            receipt.totalCost = receipt.unitCost.multiply(qty);
        }
        receipt.sellingPrice = selling;
        receipt.brand = blankToNull(str(first(body, "brand", "batch", "batchNumber")));
        receipt.batchNumber = blankToNull(str(first(body, "batchNumber", "batch_number")));
        LocalDateTime when = parseDt(str(first(body, "receivedAt", "received_at")));
        receipt.receivedAt = when == null ? LocalDateTime.now() : when;
        receipt.persist();
        return mapper.itemToFlutter(item);
    }

    @Transactional
    public Map<String, Object> adjustStock(Map<String, Object> body) {
        Long id = asLong(body.get("itemId") != null ? body.get("itemId") : body.get("id"));
        if (id == null) return null;
        StockItem item = stockItemRepository.findById(id);
        if (item == null) return null;
        BigDecimal current = item.stockQty == null ? BigDecimal.ZERO : item.stockQty;

        if (body.containsKey("stockQty") || body.containsKey("stock_qty") || body.containsKey("newQty")) {
            item.stockQty = bd(first(body, "stockQty", "stock_qty", "newQty"));
        } else if (body.containsKey("quantity") || body.containsKey("qty")) {
            BigDecimal qty = bd(first(body, "quantity", "qty"));
            if (qty == null) qty = BigDecimal.ZERO;
            String type = str(body.get("type")).toLowerCase();
            if (type.equals("remove") || type.equals("subtract") || type.equals("out")) {
                item.stockQty = current.subtract(qty);
            } else {
                item.stockQty = current.add(qty);
            }
        } else if (body.containsKey("adjustment") || body.containsKey("delta")) {
            BigDecimal delta = bd(first(body, "adjustment", "delta"));
            item.stockQty = current.add(delta == null ? BigDecimal.ZERO : delta);
        }
        return mapper.itemToFlutter(item);
    }

    @Transactional
    public List<Map<String, Object>> stockReceipts() {
        List<MobileStockReceipt> rows = MobileStockReceipt.listAll();
        rows.sort((a, b) -> b.receivedAt.compareTo(a.receivedAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileStockReceipt r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.id.intValue());
            m.put("item_id", r.itemId.intValue());
            m.put("item_name", r.itemName);
            m.put("store_id", r.storeId == null ? null : r.storeId.intValue());
            m.put("quantity", d(r.quantity));
            m.put("unit_cost", d(r.unitCost));
            m.put("total_cost", d(r.totalCost));
            m.put("selling_price", d(r.sellingPrice));
            m.put("brand", r.brand);
            m.put("batch_number", r.batchNumber);
            m.put("received_at", r.receivedAt.format(ISO));
            m.put("created_at", r.receivedAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    @Transactional
    public boolean deleteReceipt(Object idObj) {
        Long id = asLong(idObj);
        if (id == null) return false;
        return MobileStockReceipt.deleteById(id);
    }

    @Transactional
    public Map<String, Object> transferStock(Map<String, Object> body) {
        Long fromId = asLong(first(body, "fromItemId", "from_item_id"));
        Long toId = asLong(first(body, "toItemId", "to_item_id"));
        boolean destinationOnly = Boolean.TRUE.equals(body.get("destinationOnly"))
                || "true".equalsIgnoreCase(str(body.get("destinationOnly")));
        BigDecimal fromQty = bd(first(body, "fromQuantity", "from_quantity", "quantity"));
        BigDecimal toQty = bd(first(body, "toQuantity", "to_quantity"));
        if (toQty == null) toQty = fromQty;
        if (fromId == null || toId == null) {
            throw new WebApplicationException("Invalid transfer payload", 400);
        }
        if (!destinationOnly && (fromQty == null || fromQty.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new WebApplicationException("Invalid transfer payload", 400);
        }
        if (destinationOnly && (toQty == null || toQty.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new WebApplicationException("Invalid transfer payload", 400);
        }
        StockItem from = stockItemRepository.findById(fromId);
        StockItem to = stockItemRepository.findById(toId);
        if (from == null || to == null) throw new WebApplicationException("Item not found", 404);

        if (!destinationOnly) {
            BigDecimal fromOnHand = mapper.resolvedStockQty(from);
            if (fromOnHand.compareTo(fromQty) < 0) {
                throw new WebApplicationException("Insufficient stock to transfer", 400);
            }
            deductStock(from, fromQty);
        }
        BigDecimal addQty = toQty == null ? BigDecimal.ZERO : toQty;
        BigDecimal toOnHand = to.stockQty == null ? BigDecimal.ZERO : to.stockQty;
        to.stockQty = toOnHand.add(addQty);

        BigDecimal unitCost = bd(first(body, "toCostPrice", "unitCost", "unit_cost", "destUnitCost"));
        BigDecimal selling = bd(first(body, "toSellingPrice", "sellingPrice", "selling_price", "destSellingPrice"));
        if (unitCost != null && unitCost.compareTo(BigDecimal.ZERO) > 0) to.costPrice = unitCost;
        if (selling != null && selling.compareTo(BigDecimal.ZERO) > 0) to.sellingPrice = selling;

        MobileStockTransfer t = new MobileStockTransfer();
        t.fromItemId = fromId;
        t.toItemId = toId;
        t.fromItemName = nz(from.stockItemName);
        t.toItemName = nz(to.stockItemName);
        t.fromQuantity = fromQty == null ? BigDecimal.ZERO : fromQty;
        t.toQuantity = addQty;
        t.unitCost = unitCost;
        t.sellingPrice = selling;
        t.note = blankToNull(str(first(body, "note", "notes")));
        t.createdAt = LocalDateTime.now();
        t.persist();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Transfer recorded");
        out.put("id", t.id.intValue());
        return out;
    }

    @Transactional
    public List<Map<String, Object>> stockTransfers() {
        List<MobileStockTransfer> rows = MobileStockTransfer.listAll();
        rows.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileStockTransfer t : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.id.intValue());
            m.put("from_item_id", t.fromItemId.intValue());
            m.put("to_item_id", t.toItemId.intValue());
            m.put("from_item_name", t.fromItemName);
            m.put("to_item_name", t.toItemName);
            m.put("from_quantity", d(t.fromQuantity));
            m.put("to_quantity", d(t.toQuantity));
            m.put("unit_cost", d(t.unitCost));
            m.put("selling_price", d(t.sellingPrice));
            m.put("note", t.note);
            m.put("created_at", t.createdAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    // -------------------- Debts --------------------

    @Transactional
    public List<Map<String, Object>> debts(Boolean isPaid) {
        List<MobileDebt> rows = MobileDebt.listAll();
        rows.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileDebt d : rows) {
            if (isPaid != null && d.paid != isPaid) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.id.intValue());
            m.put("store_id", d.storeId == null ? null : d.storeId.intValue());
            m.put("sale_id", d.saleId == null ? null : d.saleId.intValue());
            m.put("customer_name", d.customerName);
            m.put("phone", d.phone);
            m.put("address", d.address);
            m.put("amount", d(d.amount));
            m.put("is_paid", d.paid ? 1 : 0);
            m.put("created_at", d.createdAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> payDebt(Map<String, Object> body) {
        Long debtId = asLong(first(body, "debtId", "debt_id", "id"));
        BigDecimal amount = bd(body.get("amount"));
        if (debtId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WebApplicationException("Invalid debt payment", 400);
        }
        MobileDebt debt = MobileDebt.findById(debtId);
        if (debt == null) throw new WebApplicationException("Debt not found", 404);
        if (debt.paid) throw new WebApplicationException("Debt already paid", 400);

        BigDecimal remaining = debt.amount.subtract(amount);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            remaining = BigDecimal.ZERO;
            debt.amount = BigDecimal.ZERO;
            debt.paid = true;
        } else {
            debt.amount = remaining;
        }

        MobileDebtPayment p = new MobileDebtPayment();
        p.debtId = debt.id;
        p.saleId = debt.saleId;
        p.customerName = debt.customerName;
        p.amount = amount;
        p.note = blankToNull(str(body.get("note")));
        p.createdAt = LocalDateTime.now();
        p.persist();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Debt payment recorded");
        out.put("remaining", d(remaining));
        out.put("id", p.id.intValue());
        return out;
    }

    @Transactional
    public List<Map<String, Object>> debtPayments(String customerName) {
        List<MobileDebtPayment> rows = MobileDebtPayment.list("reversed", false);
        rows.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        String needle = customerName == null ? "" : customerName.trim().toLowerCase();
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileDebtPayment p : rows) {
            if (!needle.isEmpty() && !str(p.customerName).toLowerCase().contains(needle)) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.id.intValue());
            m.put("debt_id", p.debtId == null ? null : p.debtId.intValue());
            m.put("sale_id", p.saleId == null ? null : p.saleId.intValue());
            m.put("customer_name", p.customerName);
            m.put("amount", d(p.amount));
            m.put("note", p.note);
            m.put("created_at", p.createdAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> reverseDebtPayment(Map<String, Object> body) {
        Long id = asLong(first(body, "id", "paymentId", "payment_id"));
        if (id == null) throw new WebApplicationException("payment id required", 400);
        MobileDebtPayment p = MobileDebtPayment.findById(id);
        if (p == null || p.reversed) throw new WebApplicationException("Payment not found", 404);
        p.reversed = true;
        if (p.debtId != null) {
            MobileDebt debt = MobileDebt.findById(p.debtId);
            if (debt != null) {
                debt.amount = (debt.amount == null ? BigDecimal.ZERO : debt.amount).add(p.amount);
                debt.paid = false;
            }
        }
        return Map.of("message", "Payment reversed");
    }

    // -------------------- Loans --------------------

    @Transactional
    public List<Map<String, Object>> loans() {
        List<MobileLoan> rows = MobileLoan.listAll();
        rows.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileLoan l : rows) out.add(loanToMap(l));
        return out;
    }

    @Transactional
    public Map<String, Object> saveLoan(Map<String, Object> body) {
        Long clientId = asLong(first(body, "clientId", "client_id"));
        BigDecimal principal = bd(first(body, "principalAmount", "principal_amount", "amount"));
        if (clientId == null || principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WebApplicationException("Invalid loan payload", 400);
        }
        Long id = asLong(body.get("id"));
        MobileLoan loan = id == null ? new MobileLoan() : MobileLoan.findById(id);
        if (loan == null) loan = new MobileLoan();
        loan.storeId = asLong(first(body, "storeId", "store_id"));
        loan.clientId = clientId;
        loan.principalAmount = principal;
        loan.annualInterestPercent = bd(first(body, "annualInterestPercent", "annual_interest_percent"));
        if (loan.annualInterestPercent == null) loan.annualInterestPercent = BigDecimal.ZERO;
        loan.interestType = str(first(body, "interestType", "interest_type"));
        if (loan.interestType.isBlank()) loan.interestType = "simple";
        loan.expectedPaymentDate = parseDate(str(first(body, "expectedPaymentDate", "expected_payment_date")));
        if (loan.expectedPaymentDate == null) loan.expectedPaymentDate = LocalDate.now().plusMonths(1);
        loan.note = blankToNull(str(body.get("note")));
        loan.status = str(body.get("status"));
        if (loan.status.isBlank()) loan.status = "active";
        if (loan.createdAt == null) loan.createdAt = LocalDateTime.now();

        long days = ChronoUnit.DAYS.between(loan.createdAt.toLocalDate(), loan.expectedPaymentDate);
        if (days < 0) days = 0;
        double r = loan.annualInterestPercent.doubleValue() / 100.0;
        double interest;
        if ("compounding".equalsIgnoreCase(loan.interestType)) {
            interest = principal.doubleValue() * (Math.pow(1.0 + r / 365.0, days) - 1.0);
        } else {
            interest = principal.doubleValue() * r * (days / 365.0);
        }
        loan.interestAmount = BigDecimal.valueOf(interest).setScale(2, RoundingMode.HALF_UP);
        loan.totalDue = principal.add(loan.interestAmount);
        if (loan.id == null) loan.persist();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Loan saved");
        out.put("id", loan.id.intValue());
        out.put("data", loanToMap(loan));
        return out;
    }

    @Transactional
    public List<Map<String, Object>> loanPayments(Long clientId) {
        List<MobileLoanPayment> rows = MobileLoanPayment.listAll();
        rows.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileLoanPayment p : rows) {
            if (clientId != null && (p.clientId == null || !p.clientId.equals(clientId))) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.id.intValue());
            m.put("loan_id", p.loanId.intValue());
            m.put("client_id", p.clientId == null ? null : p.clientId.intValue());
            m.put("store_id", p.storeId == null ? null : p.storeId.intValue());
            m.put("amount", d(p.amount));
            m.put("note", p.note);
            m.put("created_at", p.createdAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> saveLoanPayment(Map<String, Object> body) {
        Long loanId = asLong(first(body, "loanId", "loan_id"));
        BigDecimal amount = bd(body.get("amount"));
        if (loanId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WebApplicationException("Invalid loan payment", 400);
        }
        MobileLoan loan = MobileLoan.findById(loanId);
        if (loan == null) throw new WebApplicationException("Loan not found", 404);
        loan.amountPaid = (loan.amountPaid == null ? BigDecimal.ZERO : loan.amountPaid).add(amount);
        BigDecimal remaining = (loan.totalDue == null ? BigDecimal.ZERO : loan.totalDue).subtract(loan.amountPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            remaining = BigDecimal.ZERO;
            loan.status = "paid";
        }
        MobileLoanPayment p = new MobileLoanPayment();
        p.loanId = loanId;
        p.clientId = asLong(first(body, "clientId", "client_id"));
        if (p.clientId == null) p.clientId = loan.clientId;
        p.storeId = asLong(first(body, "storeId", "store_id"));
        p.amount = amount;
        p.note = blankToNull(str(body.get("note")));
        p.createdAt = LocalDateTime.now();
        p.persist();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Loan payment recorded");
        out.put("remaining", d(remaining));
        out.put("id", p.id.intValue());
        return out;
    }

    private Map<String, Object> loanToMap(MobileLoan l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.id.intValue());
        m.put("store_id", l.storeId == null ? null : l.storeId.intValue());
        m.put("client_id", l.clientId.intValue());
        m.put("principal_amount", d(l.principalAmount));
        m.put("annual_interest_percent", d(l.annualInterestPercent));
        m.put("interest_type", l.interestType);
        m.put("expected_payment_date", l.expectedPaymentDate == null ? null : l.expectedPaymentDate.toString());
        m.put("interest_amount", d(l.interestAmount));
        m.put("total_due", d(l.totalDue));
        m.put("amount_paid", d(l.amountPaid));
        m.put("note", l.note);
        m.put("status", l.status);
        m.put("created_at", l.createdAt.format(ISO));
        return m;
    }

    // -------------------- Services --------------------

    @Transactional
    public List<Map<String, Object>> services() {
        List<MobileServiceTxn> rows = MobileServiceTxn.listAll();
        rows.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MobileServiceTxn s : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.id.intValue());
            m.put("store_id", s.storeId == null ? null : s.storeId.intValue());
            m.put("title", s.title);
            m.put("description", s.description);
            m.put("amount", d(s.amount));
            m.put("customer_name", s.customerName);
            m.put("payment_method", s.paymentMethod);
            m.put("created_at", s.createdAt.format(ISO));
            out.add(m);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> saveService(Map<String, Object> body) {
        String title = str(body.get("title"));
        BigDecimal amount = bd(body.get("amount"));
        if (title.isBlank() || amount == null) {
            throw new WebApplicationException("Invalid service payload", 400);
        }
        Long id = asLong(body.get("id"));
        MobileServiceTxn s = id == null ? new MobileServiceTxn() : MobileServiceTxn.findById(id);
        if (s == null) s = new MobileServiceTxn();
        s.storeId = asLong(first(body, "storeId", "store_id"));
        s.title = title;
        s.description = blankToNull(str(body.get("description")));
        s.amount = amount;
        s.customerName = blankToNull(str(first(body, "customerName", "customer_name")));
        s.paymentMethod = blankToNull(str(first(body, "paymentMethod", "payment_method")));
        if (s.createdAt == null) s.createdAt = LocalDateTime.now();
        if (s.id == null) s.persist();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Service saved");
        out.put("id", s.id.intValue());
        return out;
    }

    @Transactional
    public boolean deleteService(Object idObj) {
        Long id = asLong(idObj);
        if (id == null) return false;
        return MobileServiceTxn.deleteById(id);
    }

    // -------------------- helpers --------------------

    private record ParsedLine(StockItem item, BigDecimal qty, BigDecimal unitPrice, BigDecimal discount, BigDecimal lineTotal) {}

    /** Deduct qty from catalog stockQty and FIFO from batches when present. */
    private void deductStock(StockItem item, BigDecimal qty) {
        if (item == null || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal remaining = qty;
        List<StockBatch> batches = stockBatchRepository.list("stockItemId = ?1 order by id asc", item.id);
        for (StockBatch batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal onHand = batch.stockAtHand == null ? BigDecimal.ZERO : batch.stockAtHand;
            if (onHand.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal take = onHand.min(remaining);
            batch.stockAtHand = onHand.subtract(take);
            remaining = remaining.subtract(take);
        }
        BigDecimal catalog = item.stockQty == null ? BigDecimal.ZERO : item.stockQty;
        item.stockQty = catalog.subtract(qty);
        if (item.stockQty.compareTo(BigDecimal.ZERO) < 0) {
            item.stockQty = BigDecimal.ZERO;
        }
    }

    private void restoreStock(StockItem item, BigDecimal qty) {
        if (item == null || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal catalog = item.stockQty == null ? BigDecimal.ZERO : item.stockQty;
        item.stockQty = catalog.add(qty);
        StockBatch batch = stockBatchRepository.find("stockItemId", item.id).firstResult();
        if (batch != null) {
            BigDecimal onHand = batch.stockAtHand == null ? BigDecimal.ZERO : batch.stockAtHand;
            batch.stockAtHand = onHand.add(qty);
        }
    }

    private static Object first(Map<String, Object> body, String... keys) {
        if (body == null) return null;
        for (String k : keys) {
            if (body.containsKey(k) && body.get(k) != null) return body.get(k);
        }
        return null;
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String str(Object o) { return o == null ? "" : o.toString().trim(); }
    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s; }

    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString().trim()); } catch (Exception e) { return null; }
    }

    private static BigDecimal bd(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal b) return b;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(o.toString().trim()); } catch (Exception e) { return null; }
    }

    private static double d(BigDecimal v) { return v == null ? 0d : v.doubleValue(); }

    private static LocalDateTime parseDt(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return LocalDateTime.parse(raw.replace(" ", "T").substring(0, Math.min(raw.length(), 19))); }
        catch (Exception e) {
            try { return LocalDate.parse(raw.substring(0, Math.min(10, raw.length()))).atStartOfDay(); }
            catch (Exception ignored) { return null; }
        }
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return LocalDate.parse(raw.substring(0, Math.min(10, raw.length()))); }
        catch (Exception e) { return null; }
    }
}
