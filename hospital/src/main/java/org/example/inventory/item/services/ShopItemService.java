package org.example.inventory.item.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.UserAuthService;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.Item;
import org.example.inventory.item.domain.ItemCategory;
import org.example.inventory.item.domain.repositories.ItemCategoryRepository;
import org.example.inventory.item.domain.repositories.ItemRepository;
import org.example.inventory.item.services.payloads.requests.ShopItemParametersRequest;
import org.example.inventory.item.services.payloads.requests.ShopItemReceiveRequest;
import org.example.inventory.item.services.payloads.requests.ShopItemRequest;
import org.example.inventory.item.services.payloads.requests.ShopItemUpdateRequest;
import org.example.inventory.item.services.payloads.responses.FullShopItemResponse;
import org.example.inventory.item.services.payloads.responses.ItemDTO;
import org.example.inventory.item.services.payloads.responses.ItemQuantityDto;
import org.example.inventory.stock.domains.Stock;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.services.StockTrackingService;
import org.example.messages.services.AppNotificationPushService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShopItemService {

    @Inject
    ItemRepository itemRepository;

    @Inject
    ItemCategoryRepository itemCategoryRepository;

    @Inject
    Pool client;

    @Inject
    UserAuthService userAuthService;

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    StockTrackingService stockTrackingService;

    @Inject
    AppNotificationPushService appNotificationPushService;

    private static final String NOT_FOUND = "Not found!";

    private static BigDecimal nz(BigDecimal b) {
        return b != null ? b : BigDecimal.ZERO;
    }

    @Transactional
    public Item addShopItem(ShopItemRequest request) {
    
        Item shopItem = new Item();
        shopItem.title = request.title;
        shopItem.number = userAuthService.generateRandomPassword(5);
        shopItem.reOrderTo = request.reOrderTo;
        resolveAndSetCategories(shopItem, request.categoryId, request.parentCategoryId, request.subCategory);
        shopItem.description = request.description;
        shopItem.costPrice = request.costPrice != null ? request.costPrice : BigDecimal.ZERO;
        shopItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.ZERO;
        shopItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.ZERO;
        shopItem.image = request.image;
        shopItem.lastUnitOfMeasure = request.lastUnitOfMeasure;
        shopItem.lastUnitValue = request.lastUnitValue;
        shopItem.unitOfMeasure = request.unitOfMeasure;
        shopItem.reOrderLevel = request.reOrderLevel;
        shopItem.dosage = request.dosage;
        shopItem.dosageUnit = request.dosageUnit;
        shopItem.frequency = request.frequency;
        shopItem.frequencyUnit = request.frequencyUnit;
        shopItem.duration = request.duration;
        shopItem.durationUnit = request.durationUnit;
        shopItem.route = request.route;
        shopItem.creationDate = LocalDate.now();
    
        // ✅ Correct MAX query using native SQL
        Object result = itemRepository
                .getEntityManager()
                .createNativeQuery("SELECT MAX(shelfNumber) FROM Item")
                .getSingleResult();
        Integer highestShelfNumber = result != null ? ((Number) result).intValue() : null;
    
        if (highestShelfNumber == null) {
            highestShelfNumber = 0;
        }
    
        shopItem.shelfNumber = highestShelfNumber + 1;
    
        itemRepository.persist(shopItem);
        return shopItem;
    }
    



    @Transactional
    public List<Item> getAllItemsWithStockAtHandBelowReOrderLevels() {
        return itemRepository.find("stockAtHand <= reOrderLevel", Sort.descending("id"))
                .list();
    }



    @Transactional
    public List<Item> addShopItems(List<ShopItemRequest> requests) {
        List<Item> createdItems = new ArrayList<>();

        // ✅ Find the highest existing shelf number once before the loop
        Object result = itemRepository
                .getEntityManager()
                .createNativeQuery("SELECT MAX(shelfNumber) FROM Item")
                .getSingleResult();
        Integer highestShelfNumber = result != null ? ((Number) result).intValue() : null;

        if (highestShelfNumber == null) {
            highestShelfNumber = 0;
        }

        // ✅ Create and assign shelf numbers incrementally
        for (ShopItemRequest request : requests) {
            Item shopItem = new Item();
            shopItem.title = request.title;
            shopItem.number = userAuthService.generateRandomPassword(5);
            resolveAndSetCategories(shopItem, request.categoryId, request.parentCategoryId, request.subCategory);
            shopItem.lastUnitOfMeasure = request.lastUnitOfMeasure;
            shopItem.lastUnitValue = request.lastUnitValue;
            shopItem.description = request.description;
            shopItem.costPrice = request.costPrice != null ? request.costPrice : BigDecimal.valueOf(0);
            shopItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
            shopItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);
            shopItem.image = request.image;
            shopItem.unitOfMeasure = request.unitOfMeasure;
            shopItem.reOrderLevel = request.reOrderLevel;
            shopItem.dosage = request.dosage;
            shopItem.dosageUnit = request.dosageUnit;
            shopItem.frequency = request.frequency;
            shopItem.frequencyUnit = request.frequencyUnit;
            shopItem.duration = request.duration;
            shopItem.durationUnit = request.durationUnit;
            shopItem.route = request.route;
            shopItem.creationDate = LocalDate.now();

            // ✅ Assign next shelf number
            highestShelfNumber++;
            shopItem.shelfNumber = highestShelfNumber;

            itemRepository.persist(shopItem);
            createdItems.add(shopItem);
        }

        return createdItems;
    }


    @Transactional
    public void assignShelfNumbersToUnnumberedItems() {
        // Step 1: Find the highest shelf number
        Object result = itemRepository
                .getEntityManager()
                .createNativeQuery("SELECT MAX(shelfNumber) FROM Item")
                .getSingleResult();
        Integer maxShelfNumber = result != null ? ((Number) result).intValue() : null;

        if (maxShelfNumber == null) {
            maxShelfNumber = 0;
        }

        // Step 2: Find all items without a shelf number
        List<Item> unassignedItems = itemRepository.find("shelfNumber IS NULL OR shelfNumber = 0 ORDER BY id").list();

        // Step 3: Assign new shelf numbers starting from (maxShelfNumber + 1)
        int nextShelfNumber = maxShelfNumber + 1;
        for (Item item : unassignedItems) {
            item.shelfNumber = nextShelfNumber++;
        }

        // Step 4: Persist changes in one batch
        itemRepository.persist(unassignedItems);
        itemRepository.flush();

        // Step 5: Return updated items
    }




    public void updateItemStockAtHand(Stock stock, Item item) {

        // Update the invoice fields
        item.stockAtHand = stock.newQuantity ;
        item.expiryDate = stock.expiryDate;
        item.costPrice = stock.unitCostPrice;


        item.sellingPrice = stock.unitSellingPrice;
        //item.brand = stock.brand;
        item.packaging = stock.packaging;

        // Persist the updated invoice
        itemRepository.persist(item);
    }

    public void updateItemStockAtHandAfterSelling(BigDecimal quantity, Item item) {
        // Update the stock at hand
        BigDecimal before = nz(item.stockAtHand);
        item.stockAtHand = before.subtract(quantity);

        // Persist the updated item
        itemRepository.persist(item);
        appNotificationPushService.maybePushStockAlert(item, before);
    }

    public void updateItemStockAtHandBeforeUpdating(BigDecimal quantity, Item item) {
        // Update the stock at hand
        item.stockAtHand = item.stockAtHand.add(quantity);

        // Persist the updated item
        itemRepository.persist(item);
    }




    public Response updateItemStockAtHandAfterService(List<ItemQuantityDto> itemsUsed) {
        for (ItemQuantityDto dto : itemsUsed) {
            Item item = itemRepository.find("title = ?1", dto.itemName).firstResult();

            if (item != null) {
                // Subtract the quantity from stockAtHand
                BigDecimal before = nz(item.stockAtHand);
                item.stockAtHand = before.subtract(BigDecimal.valueOf(dto.quantity));
                itemRepository.persist(item);
                appNotificationPushService.maybePushStockAlert(item, before);
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Item not found: " + dto.itemName, null))
                        .build();
            }
        }

        return Response.ok(new ResponseMessage("Items used updated successfully")).build();
    }




    public void updateItemStockAtHandAfterDeleting(BigDecimal quantity, Item item) {
        // Update the stock at hand
        item.stockAtHand = item.stockAtHand.add(quantity);

        // Persist the updated item
        itemRepository.persist(item);
    }

    @Transactional
    public void updateItemStockAtHandAfterSelling(BigDecimal quantity, StockBatch batch) {
        if (batch == null || quantity == null) {
            return;
        }
        BigDecimal hand = nz(batch.stockAtHand);
        batch.stockAtHand = hand.subtract(quantity);
        stockBatchRepository.persist(batch);
    }

    @Transactional
    public void updateItemStockAtHandAfterDeleting(BigDecimal quantity, StockBatch batch) {
        if (batch == null || quantity == null) {
            return;
        }
        BigDecimal hand = nz(batch.stockAtHand);
        batch.stockAtHand = hand.add(quantity);
        stockBatchRepository.persist(batch);
    }

    /**
     * Decrease batch stock and record {@link org.example.inventory.stock.domains.StockTracking}.
     */
    @Transactional
    public void deductStockBatchWithTracking(StockBatch batch, BigDecimal quantity,
                                            String sourceEvent, Long referenceId, String referenceType) {
        if (batch == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal before = nz(batch.stockAtHand);
        if (before.compareTo(quantity) < 0) {
            throw new IllegalStateException("Insufficient stock: need " + quantity + ", available " + before);
        }
        batch.stockAtHand = before.subtract(quantity);
        stockBatchRepository.persist(batch);
        stockTrackingService.recordBatchMovement(batch, before, batch.stockAtHand,
                StockTrackingService.TX_OUT, quantity, sourceEvent, referenceId, referenceType);
    }

    /**
     * Increase batch stock and record {@link org.example.inventory.stock.domains.StockTracking}.
     */
    @Transactional
    public void addStockBatchWithTracking(StockBatch batch, BigDecimal quantity,
                                         String sourceEvent, Long referenceId, String referenceType) {
        if (batch == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal before = nz(batch.stockAtHand);
        batch.stockAtHand = before.add(quantity);
        stockBatchRepository.persist(batch);
        stockTrackingService.recordBatchMovement(batch, before, batch.stockAtHand,
                StockTrackingService.TX_IN, quantity, sourceEvent, referenceId, referenceType);
    }

    @Transactional
    public Response generateAndReturnPdf(ShopItemParametersRequest request) {
        //List<ShopItem> shopItems = shopItemRepository.listAll(Sort.ascending("category", "title"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);

            Document document = new Document(pdfDocument);

            Table table = new Table(6);
            table.setWidth(UnitValue.createPercentValue(100));

            Cell[] headerCells = {
                    createCell("Number"),
                    createCell("Category"),
                    createCell("Title"),
                    createCell("Description"),
                    createCell("CostPrice"),
                    createCell("Creation Date")

            };

            for (Cell cell : headerCells) {
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            for (FullShopItemResponse item : getShopItemsAdvancedFilter(request)) {
                table.addCell(createCell(item.number));
                table.addCell(createCell(item.category != null ? item.category : ""));
                table.addCell(createCell(item.title));
                table.addCell(createCell(item.description != null ? item.description : ""));
                table.addCell(createCell("$" + (item.costPrice != null ? item.costPrice.toString() : "")));
                table.addCell(createCell(item.creationDate != null ? item.creationDate.toString() : ""));
            }

            document.add(table);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=shop_items.pdf")
                    .type("application/pdf")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Cell createCell(String content) {
        return new Cell().add(new Paragraph(content));
    }


    public List<FullShopItemResponse> getShopItemsAdvancedFilter(ShopItemParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
        SELECT
            i.id,
            ic.name AS category,
            i.subCategory,
            i.number,
            i.image,
            i.title,
            i.costPrice,
            i.sellingPrice,
            i.creationDate,
            i.unitOfMeasure,
            i.description,
            i.dosage,
            i.dosageUnit,
            i.frequency,
            i.frequencyUnit,
            i.duration,
            i.durationUnit,
            i.route
        FROM Item i
        LEFT JOIN ItemCategoryTable ic ON i.category_id = ic.id
        %s
        ORDER BY i.creationDate DESC;
        """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList()
                .await()
                .indefinitely();
    }


    private FullShopItemResponse from(Row row){

        FullShopItemResponse response = new FullShopItemResponse();
        response.id = row.getLong("id");
        response.description = row.getString("description");
        response.image = row.getString("image");
        response.number = row.getString("number");
        response.category = row.getString("category");
        response.subCategory = row.getString("subCategory");
        response.title = row.getString("title");
        response.costPrice = row.getBigDecimal("costPrice");
        response.sellingPrice = row.getBigDecimal("sellingPrice");
        response.unitOfMeasure = row.getString("unitOfMeasure");
        response.creationDate = row.getLocalDate("creationDate");
        response.dosage = row.getBigDecimal("dosage");
        response.dosageUnit = row.getString("dosageUnit");
        response.frequency = row.getBigDecimal("frequency");
        response.frequencyUnit = row.getString("frequencyUnit");
        response.duration = row.getBigDecimal("duration");
        response.durationUnit = row.getString("durationUnit");
        response.route = row.getString("route");

        return response;
    }

    private StringJoiner getStringJoiner(ShopItemParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.category != null && !request.category.isEmpty()) {
            conditions.add("ic.name = '" + request.category.replace("'", "''") + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.subCategory != null && !request.subCategory.isEmpty()) {
            conditions.add("i.subCategory = '" + request.subCategory.replace("'", "''") + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.title != null && !request.title.isEmpty()) {
            conditions.add("i.title = '" + request.title.replace("'", "''") + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("i.expiryDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }



    public List<Item> getAllShopItems() {
        return itemRepository.listAll();
    }

    @Transactional
    public List<ItemDTO> listLatestFirst() {
        return itemRepository
                .listAll(Sort.descending("id"))
                .stream()
                .map(ItemDTO::new) // or use a custom mapper: item -> new ItemDTO(item)
                .collect(Collectors.toList());
    }


    public Item getShopItemById(Long id){
        return itemRepository.findById(id);
    }

    public void deleteAllShopItems(){
        itemRepository.deleteAll();

    }

    public List<ItemDTO> getDrugItems(){
        List<Item> items = Item.find(
                "category.name = ?1 ORDER BY id DESC",
                "drug"
        ).list();

        // Convert ProcedureRequested entities to ProcedureDTO
        return items.stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response deleteShopItemById(Long id){
        Item item = itemRepository.findById(id);
        if (item == null) {
            //return Response.status(Response.Status.NOT_FOUND).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("item not found", null))
                    .build();
        }
        itemRepository.delete(item);
        return Response.ok(new ResponseMessage("Item Deleted successfully")).build();
    }


    public List<Item> searchItems(String category, String title) {
        if (category != null && title != null) {
            return itemRepository.list("category.name = ?1 AND title = ?2", category, title);
        } else if (category != null) {
            return itemRepository.list("category.name = ?1", category);
        } else if (title != null) {
            return itemRepository.list("title = ?1", title);
        } else {
            return itemRepository.listAll();
        }
    }


    public ItemDTO receiveStock(ShopItemReceiveRequest request) {
        if (request == null || request.itemId == null) {
            throw new WebApplicationException("itemId is required", 400);
        }
        Item item = itemRepository.findById(request.itemId);
        if (item == null) {
            throw new WebApplicationException(NOT_FOUND, 404);
        }
        BigDecimal qty = request.quantityReceived != null ? request.quantityReceived : BigDecimal.ZERO;
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WebApplicationException("quantityReceived must be greater than zero", 400);
        }
        item.stockAtHand = nz(item.stockAtHand).add(qty);
        if (request.unitCostPrice != null) {
            item.costPrice = request.unitCostPrice;
        } else if (request.costPrice != null) {
            item.costPrice = request.costPrice;
        }
        if (request.unitSellingPrice != null) {
            item.sellingPrice = request.unitSellingPrice;
        } else if (request.sellingPrice != null) {
            item.sellingPrice = request.sellingPrice;
        }
        if (request.expiryDate != null) {
            item.expiryDate = request.expiryDate;
        }
        if (request.packaging != null && !request.packaging.isBlank()) {
            item.packaging = request.packaging;
        }
        if (request.batchNumber != null && !request.batchNumber.isBlank()) {
            item.batchNumber = request.batchNumber;
        }
        itemRepository.persist(item);
        return new ItemDTO(item);
    }

    public ItemDTO updateShopItemById(Long id, ShopItemUpdateRequest request) {
        return itemRepository.findByIdOptional(id)
                .map(shopItem -> {

                    shopItem.title = request.title;
                    shopItem.description = request.description;
                    shopItem.reOrderTo = request.reOrderTo;
                    resolveAndSetCategories(shopItem, request.categoryId, request.parentCategoryId, request.subCategory);
                    shopItem.lastUnitOfMeasure = request.lastUnitOfMeasure;
                    shopItem.lastUnitValue = request.lastUnitValue;
                    shopItem.image = request.image;
                    shopItem.reOrderLevel = request.reOrderLevel;
                    shopItem.unitOfMeasure = request.unitOfMeasure;
                    shopItem.costPrice = request.costPrice != null ? request.costPrice : shopItem.costPrice;
                    shopItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
                    shopItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);
                    shopItem.dosage = request.dosage;
                    shopItem.dosageUnit = request.dosageUnit;
                    shopItem.frequency = request.frequency;
                    shopItem.frequencyUnit = request.frequencyUnit;
                    shopItem.duration = request.duration;
                    shopItem.durationUnit = request.durationUnit;
                    shopItem.route = request.route;

                    itemRepository.persist(shopItem);

                    return new ItemDTO(shopItem);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


        private StringJoiner getStringJoinerSimplified(ShopItemParametersRequest request) {
            AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

            Map<String, String> searchCriteria = new HashMap<>();
            searchCriteria.put("category", request.category);
            searchCriteria.put("title", request.title);
            searchCriteria.put("datefrom", String.valueOf(request.datefrom));
            searchCriteria.put("dateto", String.valueOf(request.dateto));


            StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

            searchCriteria.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    if ("datefrom".equals(key) || "dateto".equals(key)) {
                        whereClause.add("creationDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
                    } else {
                        whereClause.add(key + " = '" + value + "'");
                    }
                    hasSearchCriteria.set(Boolean.TRUE);
                }
            });
            if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
                whereClause.add("1 = 1");
            }
            return whereClause;
        }

    private void resolveAndSetCategories(Item item, Long categoryId, Long parentCategoryId, String subCategoryLabel) {
        item.subCategory = subCategoryLabel;
        if (categoryId != null) {
            ItemCategory cat = itemCategoryRepository.findById(categoryId);
            if (cat != null) {
                item.category = cat;
            } else {
                item.category = null;
            }
        } else {
            item.category = null;
        }
        if (parentCategoryId != null) {
            ItemCategory parent = itemCategoryRepository.findById(parentCategoryId);
            if (parent != null) {
                item.parentCategory = parent;
            } else {
                item.parentCategory = null;
            }
        } else {
            item.parentCategory = null;
        }
    }
    }

