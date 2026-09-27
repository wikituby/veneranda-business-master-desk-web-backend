package org.example.business.mobile;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Flutter mother-app data contract under /health_care/{items,stores,clients,sales,...}.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Business Mobile API", description = "Flutter remote business endpoints")
public class BusinessMobileApiController {

    @Inject
    BusinessMobileMapper mapper;

    @Inject
    BusinessMobileFinanceService finance;

    // -------------------- Items --------------------

    @GET
    @Path("/items")
    @Operation(summary = "List products for Flutter")
    public Response listItems() {
        return Response.ok(mapper.dataEnvelope(mapper.allItemsFlutter())).build();
    }

    @POST
    @Path("/items")
    @Transactional
    public Response saveItem(Map<String, Object> body) {
        try {
            Map<String, Object> saved = mapper.upsertItemFromFlutter(body);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("message", "Item saved");
            out.put("id", saved.get("id"));
            out.put("data", saved);
            return Response.ok(out).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(Map.of("message", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/items")
    @Transactional
    public Response updateItem(Map<String, Object> body) {
        return saveItem(body);
    }

    @POST
    @Path("/items/delete")
    @Transactional
    public Response deleteItem(Map<String, Object> body) {
        boolean ok = mapper.deleteItem(body == null ? null : body.get("id"));
        if (!ok) {
            return Response.status(404).entity(Map.of("message", "Item not found")).build();
        }
        return Response.ok(Map.of("message", "Item deleted")).build();
    }

    @GET
    @Path("/items/stock-take")
    public Response stockTakeList() {
        return Response.ok(mapper.dataEnvelope(mapper.allItemsFlutter())).build();
    }

    @POST
    @Path("/items/stock-take/stock-qty")
    @Transactional
    public Response stockTakeQty(Map<String, Object> body) {
        Map<String, Object> saved = mapper.updateStockQtyFromFlutter(body);
        if (saved == null) {
            return Response.status(404).entity(Map.of("message", "Item not found")).build();
        }
        return Response.ok(mapper.dataEnvelope(mapper.allItemsFlutter())).build();
    }

    @POST
    @Path("/items/stock-take/mark-counted")
    @Transactional
    public Response markCounted(Map<String, Object> body) {
        return Response.ok(mapper.dataEnvelope(mapper.allItemsFlutter())).build();
    }

    @GET
    @Path("/items/special-list")
    public Response specialList() {
        return Response.ok(mapper.dataEnvelope(List.of())).build();
    }

    @POST
    @Path("/items/special-list")
    public Response specialListAdd(Map<String, Object> body) {
        return Response.ok(Map.of("message", "Special item noted")).build();
    }

    @POST
    @Path("/items/special-list/remove")
    public Response specialListRemove(Map<String, Object> body) {
        return Response.ok(Map.of("message", "Special item removed")).build();
    }

    @POST
    @Path("/items/special-sale-outcomes")
    public Response specialSaleOutcomes(Map<String, Object> body) {
        return Response.ok(Map.of("message", "Recorded")).build();
    }

    @GET
    @Path("/items/transactions")
    public Response itemTransactions(@QueryParam("itemId") Long itemId) {
        return Response.ok(mapper.dataEnvelope(List.of())).build();
    }

    // -------------------- Stores --------------------

    @GET
    @Path("/stores")
    public Response listStores() {
        List<Map<String, Object>> stores = mapper.allStoresFlutter();
        if (stores.isEmpty()) {
            stores = List.of(Map.of(
                    "id", 1,
                    "name", "Main Store",
                    "description", "Default store",
                    "is_default", 1,
                    "created_at", java.time.LocalDateTime.now().toString()
            ));
        }
        return Response.ok(mapper.dataEnvelope(stores)).build();
    }

    @POST
    @Path("/stores")
    @Transactional
    public Response saveStore(Map<String, Object> body) {
        try {
            Map<String, Object> saved = mapper.upsertStoreFromFlutter(body);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("message", "Store saved");
            out.put("id", saved.get("id"));
            out.put("data", saved);
            return Response.ok(out).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(Map.of("message", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/stores")
    @Transactional
    public Response updateStore(Map<String, Object> body) {
        return saveStore(body);
    }

    // -------------------- Clients --------------------

    @GET
    @Path("/clients")
    public Response listClients() {
        return Response.ok(mapper.dataEnvelope(mapper.allClientsFlutter())).build();
    }

    @GET
    @Path("/clients/refresh")
    public Response refreshClients() {
        return listClients();
    }

    @POST
    @Path("/clients")
    @Transactional
    public Response saveClient(Map<String, Object> body) {
        try {
            Map<String, Object> saved = mapper.upsertClientFromFlutter(body);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("message", "Client saved");
            out.put("id", saved.get("id"));
            out.put("data", saved);
            return Response.ok(out).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(Map.of("message", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/clients")
    @Transactional
    public Response updateClient(Map<String, Object> body) {
        return saveClient(body);
    }

    @GET
    @Path("/clients/account")
    public Response clientAccount(@QueryParam("clientId") Long clientId) {
        return Response.ok(Map.of("balance", 0)).build();
    }

    @GET
    @Path("/clients/account/transactions")
    public Response clientAccountTx(@QueryParam("clientId") Long clientId) {
        return Response.ok(mapper.dataEnvelope(List.of())).build();
    }

    @POST
    @Path("/clients/account/transaction")
    public Response clientAccountPost(Map<String, Object> body) {
        return Response.ok(Map.of("message", "Transaction recorded")).build();
    }

    // -------------------- Units --------------------

    @GET
    @Path("/units")
    public Response listUnits() {
        return Response.ok(mapper.dataEnvelope(mapper.defaultUnitsFlutter())).build();
    }

    @POST
    @Path("/units")
    public Response saveUnit(Map<String, Object> body) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("message", "Unit saved");
        out.put("id", body == null ? 1 : body.getOrDefault("id", 1));
        return Response.ok(out).build();
    }

    @POST
    @Path("/units/delete")
    public Response deleteUnit(Map<String, Object> body) {
        return Response.ok(Map.of("message", "Unit deleted")).build();
    }

    // -------------------- Stock --------------------

    @POST
    @Path("/stock/receive")
    public Response receiveStock(Map<String, Object> body) {
        try {
            Map<String, Object> saved = finance.receiveStock(body);
            if (saved == null) {
                return Response.status(404).entity(Map.of("message", "Item not found")).build();
            }
            return Response.ok(Map.of("message", "Stock received", "data", saved)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @POST
    @Path("/stock/adjust")
    public Response adjustStock(Map<String, Object> body) {
        Map<String, Object> saved = finance.adjustStock(body);
        if (saved == null) {
            return Response.status(404).entity(Map.of("message", "Item not found")).build();
        }
        return Response.ok(Map.of("message", "Stock adjusted", "data", saved)).build();
    }

    @GET
    @Path("/stock/receipts")
    public Response stockReceipts() {
        return Response.ok(mapper.dataEnvelope(finance.stockReceipts())).build();
    }

    @POST
    @Path("/stock/receipts/delete")
    public Response deleteReceipt(Map<String, Object> body) {
        finance.deleteReceipt(body == null ? null : body.get("id"));
        return Response.ok(Map.of("message", "Receipt deleted")).build();
    }

    @GET
    @Path("/stock/transfers")
    public Response stockTransfers() {
        return Response.ok(mapper.dataEnvelope(finance.stockTransfers())).build();
    }

    @POST
    @Path("/stock/transfer")
    public Response stockTransfer(Map<String, Object> body) {
        return doStockTransfer(body);
    }

    @POST
    @Path("/stock/transfers")
    public Response stockTransfersPost(Map<String, Object> body) {
        return doStockTransfer(body);
    }

    private Response doStockTransfer(Map<String, Object> body) {
        try {
            return Response.ok(finance.transferStock(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    // -------------------- Sales --------------------

    @GET
    @Path("/sales/history")
    public Response salesHistory(
            @QueryParam("start") String start,
            @QueryParam("end") String end) {
        return Response.ok(mapper.dataEnvelope(finance.salesHistory(start, end))).build();
    }

    @GET
    @Path("/sales/by-customer")
    public Response salesByCustomer(@QueryParam("name") String name) {
        return Response.ok(mapper.dataEnvelope(finance.salesByCustomer(name))).build();
    }

    @POST
    @Path("/sales")
    public Response createSale(Map<String, Object> body) {
        try {
            return Response.status(201).entity(finance.createSale(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @POST
    @Path("/sales/delete")
    public Response deleteSale(Map<String, Object> body) {
        boolean ok = finance.deleteSale(body == null ? null : body.get("id"));
        if (!ok) {
            return Response.status(404).entity(Map.of("message", "Sale not found")).build();
        }
        return Response.ok(Map.of("message", "Sale deleted")).build();
    }

    @POST
    @Path("/sales/reverse-initial-payment")
    public Response reverseSalePayment(Map<String, Object> body) {
        try {
            finance.reverseInitialPayment(body == null ? null : (body.get("saleId") != null ? body.get("saleId") : body.get("id")));
            return Response.ok(Map.of("message", "Payment reversed")).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    // -------------------- Expenses --------------------

    @GET
    @Path("/expenses")
    public Response listExpenses() {
        return Response.ok(mapper.dataEnvelope(finance.allExpenses())).build();
    }

    @POST
    @Path("/expenses")
    public Response saveExpense(Map<String, Object> body) {
        try {
            return Response.ok(finance.saveExpense(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @POST
    @Path("/expenses/delete")
    public Response deleteExpense(Map<String, Object> body) {
        finance.deleteExpense(body == null ? null : body.get("id"));
        return Response.ok(Map.of("message", "Expense deleted")).build();
    }

    @GET
    @Path("/expense-categories")
    public Response expenseCategories() {
        return Response.ok(mapper.dataEnvelope(finance.expenseCategories())).build();
    }

    @POST
    @Path("/expense-categories")
    public Response saveExpenseCategory(Map<String, Object> body) {
        try {
            return Response.ok(finance.saveExpenseCategory(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/expense-accounts")
    public Response expenseAccounts() {
        return Response.ok(mapper.dataEnvelope(finance.expenseAccounts())).build();
    }

    @POST
    @Path("/expense-accounts")
    public Response saveExpenseAccount(Map<String, Object> body) {
        try {
            return Response.ok(finance.saveExpenseAccount(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    // -------------------- Debts --------------------

    @GET
    @Path("/debts")
    public Response listDebts(@QueryParam("isPaid") Boolean isPaid) {
        return Response.ok(mapper.dataEnvelope(finance.debts(isPaid))).build();
    }

    @POST
    @Path("/debts/pay")
    public Response payDebt(Map<String, Object> body) {
        try {
            return Response.ok(finance.payDebt(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/debt-payments")
    public Response debtPayments(@QueryParam("customerName") String customerName) {
        return Response.ok(mapper.dataEnvelope(finance.debtPayments(customerName))).build();
    }

    @POST
    @Path("/debt-payments/reverse")
    public Response reverseDebtPayment(Map<String, Object> body) {
        try {
            return Response.ok(finance.reverseDebtPayment(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    // -------------------- Loans --------------------

    @GET
    @Path("/loans")
    public Response listLoans() {
        return Response.ok(mapper.dataEnvelope(finance.loans())).build();
    }

    @POST
    @Path("/loans")
    public Response saveLoan(Map<String, Object> body) {
        try {
            return Response.ok(finance.saveLoan(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/loans/payments")
    public Response loanPayments(@QueryParam("clientId") Long clientId) {
        return Response.ok(mapper.dataEnvelope(finance.loanPayments(clientId))).build();
    }

    @POST
    @Path("/loans/payments")
    public Response saveLoanPayment(Map<String, Object> body) {
        try {
            return Response.ok(finance.saveLoanPayment(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    // -------------------- Services --------------------

    @GET
    @Path("/services")
    public Response listServices() {
        return Response.ok(mapper.dataEnvelope(finance.services())).build();
    }

    @POST
    @Path("/services")
    public Response saveService(Map<String, Object> body) {
        try {
            return Response.ok(finance.saveService(body)).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @POST
    @Path("/services/delete")
    public Response deleteService(Map<String, Object> body) {
        finance.deleteService(body == null ? null : body.get("id"));
        return Response.ok(Map.of("message", "Service deleted")).build();
    }

    // -------------------- Item categories --------------------

    @GET
    @Path("/item-categories")
    public Response itemCategories(@QueryParam("type") String type) {
        return Response.ok(mapper.dataEnvelope(mapper.itemCategoryNames(type))).build();
    }

    @POST
    @Path("/item-categories")
    public Response saveItemCategory(@QueryParam("type") String type, Map<String, Object> body) {
        return Response.ok(Map.of("message", "Category saved")).build();
    }

    @POST
    @Path("/item-categories/delete")
    public Response deleteItemCategory(Map<String, Object> body) {
        return Response.ok(Map.of("message", "Category deleted")).build();
    }
}
