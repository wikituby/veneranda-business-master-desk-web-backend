package org.example.cafeteria.inventory.item.services;

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
import org.example.cafeteria.inventory.item.domains.CanteenItem;
import org.example.cafeteria.inventory.stock.domains.CanteenStock;
import org.example.cafeteria.inventory.item.domains.repositories.CanteenItemRepository;
import org.example.cafeteria.inventory.item.services.payloads.requests.CanteenItemParametersRequest;
import org.example.cafeteria.inventory.item.services.payloads.requests.CanteenItemRequest;
import org.example.cafeteria.inventory.item.services.payloads.requests.CanteenItemUpdateRequest;
import org.example.cafeteria.inventory.item.services.payloads.responses.CanteenItemDTO;
import org.example.cafeteria.inventory.item.services.payloads.responses.CanteenItemQuantityDto;
import org.example.cafeteria.inventory.item.services.payloads.responses.FullCanteenItemResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
public class CanteenItemService {

    @Inject
    CanteenItemRepository canteenItemRepository;

    @Inject
    Pool client;

    @Inject
    UserAuthService userAuthService;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public CanteenItem addCanteenItem(CanteenItemRequest request) {
        CanteenItem canteenItem = new CanteenItem();
        canteenItem.title = request.title;
        canteenItem.number = userAuthService.generateRandomPassword(5);
        canteenItem.category = request.category;
        canteenItem.subCategory = request.subCategory;
        canteenItem.description = request.description;
        canteenItem.costPrice = request.costPrice != null ? request.costPrice : BigDecimal.valueOf(0);
        canteenItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
        canteenItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);

        canteenItem.image = request.image;
        canteenItem.unitOfMeasure = request.unitOfMeasure;
        canteenItem.reOrderLevel = request.reOrderLevel;

        canteenItem.creationDate = LocalDate.now();
        //shopItem.expiryDate = request.expiryDate;

        canteenItemRepository.persist(canteenItem);

        return canteenItem;

    }


    @Transactional
    public List<CanteenItem> addCanteenItems(List<CanteenItemRequest> requests) {
        List<CanteenItem> createdItems = new ArrayList<>();

        for (CanteenItemRequest request : requests) {
            CanteenItem canteenItem = new CanteenItem();
            canteenItem.title = request.title;
            canteenItem.number = userAuthService.generateRandomPassword(5);
            canteenItem.category = request.category;
            canteenItem.subCategory = request.subCategory;
            canteenItem.description = request.description;
            canteenItem.costPrice = request.costPrice != null ? request.costPrice : BigDecimal.valueOf(0);
            canteenItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
            canteenItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);

            canteenItem.image = request.image;
            canteenItem.unitOfMeasure = request.unitOfMeasure;
            canteenItem.reOrderLevel = request.reOrderLevel;
            canteenItem.creationDate = LocalDate.now();

            canteenItemRepository.persist(canteenItem);
            createdItems.add(canteenItem);
        }

        return createdItems;
    }



    public void updateCanteenItemStockAtHand(CanteenStock canteenStock, CanteenItem canteenItem) {

        // Update the invoice fields
        canteenItem.stockAtHand = canteenStock.newQuantity ;
        canteenItem.expiryDate = canteenStock.expiryDate;
        canteenItem.costPrice = canteenStock.unitCostPrice;

        canteenItem.sellingPrice = canteenStock.unitSellingPrice;
        canteenItem.brand = canteenStock.brand;
        canteenItem.packaging = canteenStock.packaging;

        // Persist the updated invoice
        canteenItemRepository.persist(canteenItem);
    }

    public void updateCanteenItemStockAtHandAfterSelling(BigDecimal quantity, CanteenItem canteenItem) {
        // Update the stock at hand
        canteenItem.stockAtHand = canteenItem.stockAtHand.subtract(quantity);

        // Persist the updated item
        canteenItemRepository.persist(canteenItem);
    }




    public Response updateCanteenItemStockAtHandAfterService(List<CanteenItemQuantityDto> canteenItemsUsed) {
        for (CanteenItemQuantityDto dto : canteenItemsUsed) {
            CanteenItem canteenItem = canteenItemRepository.find("title = ?1", dto.itemName).firstResult();

            if (canteenItem != null) {
                // Subtract the quantity from stockAtHand
                canteenItem.stockAtHand = canteenItem.stockAtHand.subtract(BigDecimal.valueOf(dto.quantity));
                canteenItemRepository.persist(canteenItem);
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Item not found: " + dto.itemName, null))
                        .build();
            }
        }

        return Response.ok(new ResponseMessage("Items used updated successfully")).build();
    }




    public void updateCanteenItemStockAtHandAfterDeleting(BigDecimal quantity, CanteenItem canteenItem) {
        // Update the stock at hand
        canteenItem.stockAtHand = canteenItem.stockAtHand.add(quantity);

        // Persist the updated item
        canteenItemRepository.persist(canteenItem);
    }



    @Transactional
    public Response generateAndReturnPdf(CanteenItemParametersRequest request) {
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

            for (FullCanteenItemResponse Item : getCanteenItemsAdvancedFilter(request)) {
                table.addCell(createCell(Item.number));
                table.addCell(createCell(Item.category));
                table.addCell(createCell(Item.title));
                table.addCell(createCell(Item.description));
                table.addCell(createCell("$" + Item.costPrice.toString()));
                table.addCell(createCell(Item.creationDate.toString()));
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


    public List<FullCanteenItemResponse> getCanteenItemsAdvancedFilter(CanteenItemParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
        SELECT
            id,
            category,
            subCategory,
            number,
            image,
            title,
            costPrice,
            sellingPrice,
            creationDate,
            unitOfMeasure,
            description
        FROM Item
        %s
        ORDER BY creationDate DESC;
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


    private FullCanteenItemResponse from(Row row){

        FullCanteenItemResponse response = new FullCanteenItemResponse();
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

        return response;
    }

    private StringJoiner getStringJoiner(CanteenItemParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.category != null && !request.category.isEmpty()) {
            conditions.add("category = '" + request.category + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.subCategory != null && !request.subCategory.isEmpty()) {
            conditions.add("subCategory = '" + request.subCategory + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.title != null && !request.title.isEmpty()) {
            conditions.add("title = '" + request.title + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("expiryDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }



    public List<CanteenItem> getAllCanteenItems() {
        return canteenItemRepository.listAll();
    }

    @Transactional
    public List<CanteenItemDTO> listLatestFirst() {
        return canteenItemRepository
                .listAll(Sort.descending("id"))
                .stream()
                .map(CanteenItemDTO::new) // or use a custom mapper: item -> new ItemDTO(item)
                .collect(Collectors.toList());
    }


    public CanteenItem getCanteenItemById(Long id){
        return canteenItemRepository.findById(id);
    }

    public void deleteAllCanteenItems(){
        canteenItemRepository.deleteAll();

    }

    public List<CanteenItemDTO> getDrugItems(){
        List<CanteenItem> canteenItems = CanteenItem.find(
                "category = ?1 ORDER BY id DESC",
                "drug"
        ).list();

        // Convert ProcedureRequested entities to ProcedureDTO
        return canteenItems.stream()
                .map(CanteenItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response deleteCanteenItemById(Long id){
        CanteenItem canteenItem = canteenItemRepository.findById(id);
        if (canteenItem == null) {
            //return Response.status(Response.Status.NOT_FOUND).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("item not found", null))
                    .build();
        }
        canteenItemRepository.delete(canteenItem);
        return Response.ok(new ResponseMessage("Item Deleted successfully")).build();
    }


    public List<CanteenItem> searchItems(String category, String title) {
        if (category != null && title != null) {
            return canteenItemRepository.list("category = ?1 AND title = ?2", category, title);
        } else if (category != null) {
            return canteenItemRepository.list("category = ?1", category);
        } else if (title != null) {
            return canteenItemRepository.list("title = ?1", title);
        } else {
            // If both parameters are null or empty, return all items.
            return canteenItemRepository.listAll();
        }
    }


    public CanteenItemDTO updateCanteenItemById(Long id, CanteenItemUpdateRequest request) {
        return canteenItemRepository.findByIdOptional(id)
                .map(canteenItem -> {

                    canteenItem.title = request.title;
                    canteenItem.description = request.description;
                    canteenItem.category = request.category;
                    canteenItem.subCategory = request.subCategory;
                    canteenItem.image = request.image;
                    canteenItem.reOrderLevel = request.reOrderLevel;
                    canteenItem.unitOfMeasure = request.unitOfMeasure;
                    canteenItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
                    canteenItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);

                    canteenItemRepository.persist(canteenItem);

                    return new CanteenItemDTO(canteenItem);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


    private StringJoiner getStringJoinerSimplified(CanteenItemParametersRequest request) {
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
}




