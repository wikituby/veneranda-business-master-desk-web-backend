package org.example.business.mobile;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.client.domains.Patient;
import org.example.client.domains.repositories.PatientRepository;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.domains.repositories.StockItemRepository;
import org.example.inventory.store.domains.Store;
import org.example.inventory.store.domains.repositories.StoreRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps Quarkus entities into the Flutter mother-app remote payload shape.
 * Products use {@link StockItem} (same catalog as Angular POS), not legacy shop Item.
 */
@ApplicationScoped
public class BusinessMobileMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    PatientRepository patientRepository;

    public Map<String, Object> stockItemToFlutter(StockItem item) {
        return stockItemToFlutter(item, null);
    }

    public Map<String, Object> stockItemToFlutter(StockItem item, BigDecimal stockOverride) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (item == null) return m;
        BigDecimal stock = stockOverride != null ? stockOverride : resolvedStockQty(item);
        m.put("id", item.id == null ? null : item.id.intValue());
        m.put("store_id", null);
        m.put("name", nz(item.stockItemName));
        m.put("sku", firstNonBlank(item.sku, item.barcode));
        m.put("barcode", item.barcode);
        m.put("category", firstNonBlank(item.businessCategory, item.itemCategoryName, item.saleCategory));
        m.put("unit", firstNonBlank(item.unit, item.lastUnitOfSellMeasure));
        m.put("unit_short", firstNonBlank(item.unitShort, item.unit, item.lastUnitOfSellMeasure));
        m.put("shelf_number", item.shelfNumber);
        m.put("image_url", item.image);
        m.put("image_url_2", item.image2);
        m.put("image_url_3", item.image3);
        m.put("cost_price", bd(item.costPrice));
        m.put("selling_price", bd(item.sellingPrice));
        m.put("stock_qty", bd(stock));
        m.put("reorder_level", item.reorderLevel == null ? 0d : item.reorderLevel.doubleValue());
        m.put("restock_to", item.restockTo == null ? 0d : item.restockTo.doubleValue());
        m.put("special_roll_meters_total", 0);
        m.put("special_roll_meters_sold", 0);
        m.put("special_category_id", null);
        m.put("stock_hand_counted", true);
        m.put("created_at", item.creationDate == null
                ? LocalDateTime.now().format(ISO)
                : item.creationDate.atStartOfDay().format(ISO));
        m.put("accepted_barcodes", parseBarcodes(item.barcode));
        return m;
    }

    /** Alias used by finance service call sites. */
    public Map<String, Object> itemToFlutter(StockItem item) {
        return stockItemToFlutter(item);
    }

    @Transactional
    public List<Map<String, Object>> allItemsFlutter() {
        List<StockItem> items = stockItemRepository.listAll(Sort.descending("id"));
        Map<Long, BigDecimal> batchQtyByItem = batchStockTotals();
        List<Map<String, Object>> out = new ArrayList<>(items.size());
        for (StockItem item : items) {
            BigDecimal fromBatches = item.id == null ? null : batchQtyByItem.get(item.id);
            out.add(stockItemToFlutter(item, fromBatches != null ? fromBatches : resolvedStockQty(item)));
        }
        return out;
    }

    private Map<Long, BigDecimal> batchStockTotals() {
        Map<Long, BigDecimal> totals = new HashMap<>();
        List<StockBatch> batches = stockBatchRepository.listAll();
        for (StockBatch batch : batches) {
            if (batch.stockItemId == null) continue;
            BigDecimal qty = batch.stockAtHand == null ? BigDecimal.ZERO : batch.stockAtHand;
            totals.merge(batch.stockItemId, qty, BigDecimal::add);
        }
        return totals;
    }

    public BigDecimal resolvedStockQty(StockItem item) {
        if (item == null) return BigDecimal.ZERO;
        if (item.id != null) {
            List<StockBatch> batches = stockBatchRepository.list("stockItemId", item.id);
            if (!batches.isEmpty()) {
                BigDecimal sum = BigDecimal.ZERO;
                for (StockBatch b : batches) {
                    if (b.stockAtHand != null) sum = sum.add(b.stockAtHand);
                }
                return sum;
            }
        }
        return item.stockQty == null ? BigDecimal.ZERO : item.stockQty;
    }

    private static List<String> parseBarcodes(String barcode) {
        if (barcode == null || barcode.isBlank()) return List.of();
        String[] parts = barcode.split("[,;|]");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p == null ? "" : p.trim();
            if (!t.isEmpty() && !out.contains(t)) out.add(t);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> upsertItemFromFlutter(Map<String, Object> body) {
        Long id = asLong(body.get("id"));
        StockItem item = id == null ? new StockItem() : stockItemRepository.findById(id);
        if (item == null) {
            item = new StockItem();
        }
        String name = str(body.get("name"));
        if (name.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        item.stockItemName = name;
        String sku = firstNonBlank(str(body.get("sku")), str(body.get("barcode")));
        if (!sku.isBlank()) {
            item.sku = sku;
        }
        String barcode = str(body.get("barcode"));
        if (!barcode.isBlank()) {
            item.barcode = barcode;
        }
        String category = str(body.get("category"));
        if (!category.isBlank()) {
            item.businessCategory = category;
        }
        if (body.containsKey("costPrice") || body.containsKey("cost_price")) {
            item.costPrice = asBd(body.get("costPrice") != null ? body.get("costPrice") : body.get("cost_price"));
        }
        if (body.containsKey("sellingPrice") || body.containsKey("selling_price")) {
            item.sellingPrice = asBd(body.get("sellingPrice") != null ? body.get("sellingPrice") : body.get("selling_price"));
        }
        if (body.containsKey("stockQty") || body.containsKey("stock_qty")) {
            item.stockQty = asBd(body.get("stockQty") != null ? body.get("stockQty") : body.get("stock_qty"));
        }
        if (body.containsKey("reorderLevel") || body.containsKey("reorder_level")) {
            item.reorderLevel = asBd(body.get("reorderLevel") != null ? body.get("reorderLevel") : body.get("reorder_level"));
        }
        if (body.containsKey("restockTo") || body.containsKey("restock_to")) {
            item.restockTo = asBd(body.get("restockTo") != null ? body.get("restockTo") : body.get("restock_to"));
        }
        String unit = firstNonBlank(str(body.get("unit")), str(body.get("unit_short")));
        if (!unit.isBlank()) {
            item.unit = unit;
            if (item.unitShort == null || item.unitShort.isBlank()) {
                item.unitShort = unit;
            }
        }
        String unitShort = str(body.get("unit_short") != null ? body.get("unit_short") : body.get("unitShort"));
        if (!unitShort.isBlank()) {
            item.unitShort = unitShort;
        }
        String shelf = str(body.get("shelfNumber") != null ? body.get("shelfNumber") : body.get("shelf_number"));
        if (!shelf.isBlank()) {
            item.shelfNumber = shelf;
        }
        String image = str(body.get("imageUrl") != null ? body.get("imageUrl") : body.get("image_url"));
        if (!image.isBlank()) {
            item.image = image;
        }
        if (item.creationDate == null) {
            item.creationDate = LocalDate.now();
        }
        item.updateDate = LocalDate.now();
        if (item.id == null) {
            if (item.costPrice == null) item.costPrice = BigDecimal.ZERO;
            if (item.sellingPrice == null) item.sellingPrice = BigDecimal.ZERO;
            if (item.stockQty == null) item.stockQty = BigDecimal.ZERO;
            stockItemRepository.persist(item);
        }
        return stockItemToFlutter(item);
    }

    public List<Map<String, Object>> allStoresFlutter() {
        List<Store> stores = storeRepository.listAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Store s : stores) {
            out.add(storeToFlutter(s));
        }
        return out;
    }

    @Transactional
    public Map<String, Object> upsertStoreFromFlutter(Map<String, Object> body) {
        Long id = asLong(body.get("id"));
        Store store = id == null ? new Store() : storeRepository.findById(id);
        if (store == null) {
            store = new Store();
        }
        String name = str(body.get("name"));
        if (name.isBlank()) {
            throw new IllegalArgumentException("Store name is required");
        }
        store.name = name;
        if (store.id == null) {
            storeRepository.persist(store);
        }
        return storeToFlutter(store);
    }

    private Map<String, Object> storeToFlutter(Store store) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", store.id == null ? null : store.id.intValue());
        m.put("name", nz(store.name));
        m.put("description", store.description);
        m.put("is_default", 0);
        m.put("created_at", LocalDateTime.now().format(ISO));
        return m;
    }

    public List<Map<String, Object>> allClientsFlutter() {
        List<Patient> patients = patientRepository.listAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Patient p : patients) {
            out.add(clientToFlutter(p));
        }
        return out;
    }

    private Map<String, Object> clientToFlutter(Patient p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.id == null ? null : p.id.intValue());
        m.put("store_id", null);
        String name = (nz(p.patientFirstName) + " " + nz(p.patientSecondName)).trim();
        m.put("name", name.isBlank() ? ("Client #" + p.patientNo) : name);
        m.put("phone", p.patientContact);
        m.put("address", p.patientAddress);
        m.put("next_of_kin_name", p.nextOfKinName);
        m.put("next_of_kin_phone", p.nextOfKinContact);
        m.put("next_of_kin_address", p.nextOfKinAddress);
        m.put("created_at", LocalDateTime.now().format(ISO));
        return m;
    }

    @Transactional
    public Map<String, Object> upsertClientFromFlutter(Map<String, Object> body) {
        Long id = asLong(body.get("id"));
        Patient patient = id == null ? null : patientRepository.findById(id);
        if (patient == null) {
            patient = new Patient();
            patient.patientNo = patientRepository.generateNextPatientNo();
            patient.patientFileNo = "OPD" + patient.patientNo;
            patient.creationDate = LocalDate.now();
        }
        String name = str(body.get("name"));
        if (name.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        String[] parts = name.trim().split("\\s+", 2);
        patient.patientFirstName = parts[0];
        patient.patientSecondName = parts.length > 1 ? parts[1] : ".";
        patient.patientContact = str(body.get("phone"));
        patient.patientAddress = str(body.get("address"));
        patient.nextOfKinName = str(body.get("nextOfKinName") != null ? body.get("nextOfKinName") : body.get("next_of_kin_name"));
        patient.nextOfKinContact = str(body.get("nextOfKinPhone") != null ? body.get("nextOfKinPhone") : body.get("next_of_kin_phone"));
        patient.nextOfKinAddress = str(body.get("nextOfKinAddress") != null ? body.get("nextOfKinAddress") : body.get("next_of_kin_address"));
        patient.patientLastUpdatedDate = LocalDate.now();
        if (patient.id == null) {
            patientRepository.persist(patient);
        }
        return clientToFlutter(patient);
    }

    @Transactional
    public boolean deleteItem(Object idObj) {
        Long id = asLong(idObj);
        if (id == null) return false;
        return stockItemRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> updateStockQtyFromFlutter(Map<String, Object> body) {
        Long id = asLong(body.get("id") != null ? body.get("id") : body.get("itemId"));
        if (id == null) return null;
        StockItem item = stockItemRepository.findById(id);
        if (item == null) return null;
        Object qty = body.get("stockQty") != null ? body.get("stockQty") : body.get("stock_qty");
        item.stockQty = asBd(qty);
        item.updateDate = LocalDate.now();
        return stockItemToFlutter(item);
    }

    @Transactional
    public Map<String, Object> receiveStockFromFlutter(Map<String, Object> body) {
        Long id = asLong(body.get("itemId") != null ? body.get("itemId") : body.get("id"));
        if (id == null) return null;
        StockItem item = stockItemRepository.findById(id);
        if (item == null) return null;
        BigDecimal qty = asBd(body.get("qty") != null ? body.get("qty") : body.get("quantity"));
        BigDecimal current = item.stockQty == null ? BigDecimal.ZERO : item.stockQty;
        item.stockQty = current.add(qty == null ? BigDecimal.ZERO : qty);
        if (body.containsKey("costPrice") || body.containsKey("cost_price")) {
            item.costPrice = asBd(body.get("costPrice") != null ? body.get("costPrice") : body.get("cost_price"));
        }
        item.updateDate = LocalDate.now();
        return stockItemToFlutter(item);
    }

    @Transactional
    public Map<String, Object> adjustStockFromFlutter(Map<String, Object> body) {
        Long id = asLong(body.get("itemId") != null ? body.get("itemId") : body.get("id"));
        if (id == null) return null;
        StockItem item = stockItemRepository.findById(id);
        if (item == null) return null;
        if (body.containsKey("stockQty") || body.containsKey("stock_qty") || body.containsKey("newQty")) {
            Object qty = body.get("stockQty") != null ? body.get("stockQty")
                    : (body.get("stock_qty") != null ? body.get("stock_qty") : body.get("newQty"));
            item.stockQty = asBd(qty);
        } else if (body.containsKey("adjustment") || body.containsKey("delta")) {
            BigDecimal delta = asBd(body.get("adjustment") != null ? body.get("adjustment") : body.get("delta"));
            BigDecimal current = item.stockQty == null ? BigDecimal.ZERO : item.stockQty;
            item.stockQty = current.add(delta == null ? BigDecimal.ZERO : delta);
        }
        item.updateDate = LocalDate.now();
        return stockItemToFlutter(item);
    }

    public List<Map<String, Object>> defaultUnitsFlutter() {
        List<Map<String, Object>> units = new ArrayList<>();
        units.add(unitRow(1, "Piece", "pc"));
        units.add(unitRow(2, "Box", "box"));
        units.add(unitRow(3, "Pack", "pk"));
        units.add(unitRow(4, "Bottle", "btl"));
        return units;
    }

    public List<String> itemCategoryNames(String type) {
        List<StockItem> items = stockItemRepository.listAll();
        List<String> names = new ArrayList<>();
        for (StockItem item : items) {
            String cat = firstNonBlank(item.businessCategory, item.itemCategoryName, item.saleCategory);
            if (cat != null && !cat.isBlank() && !names.contains(cat)) {
                names.add(cat);
            }
        }
        return names;
    }

    private static Map<String, Object> unitRow(int id, String name, String shortName) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("unit_name", name);
        m.put("unit_short_name", shortName);
        m.put("created_at", LocalDateTime.now().format(ISO));
        return m;
    }

    public Map<String, Object> dataEnvelope(Object data) {
        Map<String, Object> m = new HashMap<>();
        m.put("data", data);
        return m;
    }

    static String nz(String s) {
        return s == null ? "" : s;
    }

    static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    static String firstNonBlank(String... values) {
        if (values == null) return "";
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return "";
    }

    static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(o.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    static BigDecimal asBd(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(o.toString().trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    static double bd(BigDecimal v) {
        return v == null ? 0d : v.doubleValue();
    }
}
