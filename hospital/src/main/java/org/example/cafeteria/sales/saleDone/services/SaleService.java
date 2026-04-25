package org.example.cafeteria.sales.saleDone.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.cafeteria.finance.invoice.services.CanteenInvoiceService;
import org.example.cafeteria.inventory.item.domains.CanteenItem;
import org.example.cafeteria.inventory.item.domains.repositories.CanteenItemRepository;
import org.example.cafeteria.inventory.item.services.CanteenItemService;
import org.example.cafeteria.sales.saleDay.domains.SaleDay;
import org.example.cafeteria.sales.saleDone.domains.Sale;
import org.example.cafeteria.sales.saleDone.domains.repositories.SaleRepository;
import org.example.cafeteria.sales.saleDone.services.payloads.requests.SaleRequest;
import org.example.cafeteria.sales.saleDone.services.payloads.responses.SaleDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class SaleService {
    @Inject
    SaleRepository saleRepository;

    @Inject
    CanteenItemService itemService;

    @Inject
    CanteenInvoiceService canteenInvoiceService;

    @Inject
    CanteenItemRepository itemRepository;

    public static final String NOT_FOUND = "Not found!";
    public Response createNewTreatmentRequested(Long id, SaleRequest request) {
        // Fetch the PatientVisit and Item in one go
        SaleDay patientVisit = SaleDay.findById(id);
        CanteenItem item = CanteenItem.findById(request.itemId);

        // Handle not found error
        if (patientVisit == null || item == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Patient or item NOT FOUND", null))
                    .build();
        }

        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        BigDecimal totalBalanceDue = canteenInvoiceService.calculateTotalBalanceDueForClosedVisits(patientVisit.patient.id);

        if (totalBalanceDue.compareTo(BigDecimal.ZERO) > 0) {
            // There is an unpaid balance — do not open a new visit
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot access any service. Patient has a debt, tell the client to first pay the debt of:  " + totalBalanceDue))
                    .build();

        }

        // Check if stock is sufficient
        if (request.quantity.compareTo(item.stockAtHand) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Stock at hand is insufficient. Available: " +
                            item.stockAtHand + ", Requested: " + request.quantity,
                            "INSUFFICIENT_STOCK"))
                    .build();
        }

        // Check if a TreatmentRequested with the same item and visit already exists
        Sale existingTreatment = Sale.find(
                "visit.id = ?1 and itemName = ?2",
                patientVisit.id,
                item.title
        ).firstResult();

        SaleDTO dto;

        if (existingTreatment != null) {
            // If it exists, increment the quantity and update the total amount
            existingTreatment.quantity = existingTreatment.quantity.add(request.quantity);
            existingTreatment.totalAmount = existingTreatment.quantity.multiply(existingTreatment.unitSellingPrice);
            saleRepository.persist(existingTreatment);

            itemService.updateCanteenItemStockAtHandAfterSelling(request.quantity, item);

            dto = new SaleDTO(existingTreatment);
            return Response.ok(new ResponseMessage("Treatment request updated successfully", dto)).build();
        } else {
            // Otherwise, create a new TreatmentRequested record
            Sale sale = new Sale();
            sale.quantity = request.quantity;
            sale.status = "pending";
            sale.unitSellingPrice = item.sellingPrice;
            sale.totalAmount = request.quantity.multiply(item.sellingPrice);
            sale.saleDay = patientVisit;
            sale.itemName = item.title;
            saleRepository.persist(sale);
            itemService.updateCanteenItemStockAtHandAfterSelling(request.quantity, item);

            dto = new SaleDTO(sale);
            return Response.ok(new ResponseMessage("New treatment request created successfully", dto)).build();
        }
    }





    public List<SaleDTO> getTreatmentRequestedByVisit(Long visitId) {
        List<Sale> treatmentGive = Sale.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        // Convert the results to a list of TreatmentRequestedDTO
        return treatmentGive.stream()
                .map(SaleDTO::new)
                .toList();
    }

    @Transactional
    public Response deleteTreatmentRequestById(Long id) {

        Sale sale = saleRepository.findById(id);

        if (sale == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        saleRepository.delete(sale);

        String itemTitle = sale.itemName;

        CanteenItem item = itemRepository.find("title", itemTitle).firstResult();


        itemService.updateCanteenItemStockAtHandAfterDeleting(sale.quantity, item);


        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

}





