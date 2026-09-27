package org.example.hmis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.example.hmis.domains.HmisTracerItem;
import org.example.hmis.services.payloads.HmisTracerItemDTO;
import org.example.hmis.services.payloads.HmisTracerItemUpdateRequest;
import org.example.inventory.item.domain.Item;
import org.example.inventory.stock.domains.StockItem;
import org.example.treatment.domains.TreatmentRequested;

@ApplicationScoped
public class HmisTracerItemService {

    @Inject Hmis033bAggregationService hmis033bAggregationService;

    public List<HmisTracerItemDTO> listAll(LocalDate from, LocalDate to) {
        return HmisTracerItem.find("order by sortOrder, id").list().stream()
                .map(row -> enrich((HmisTracerItem) row, from, to))
                .toList();
    }

    public List<HmisTracerItemDTO> listActive() {
        return listAll(null, null);
    }

    @Transactional
    public HmisTracerItemDTO update(Long id, HmisTracerItemUpdateRequest request) {
        HmisTracerItem row = HmisTracerItem.findById(id);
        if (row == null) {
            throw new NotFoundException("Tracer item not found");
        }
        if (request != null) {
            row.stockItemId = request.stockItemId;
            row.shopItemId = request.shopItemId;
            if (request.active != null) {
                row.active = request.active;
            }
        }
        row.persist();
        return enrich(row, null, null);
    }

    private HmisTracerItemDTO enrich(HmisTracerItem row, LocalDate from, LocalDate to) {
        HmisTracerItemDTO dto = HmisTracerItemDTO.from(row);
        if (row.stockItemId != null) {
            StockItem stockItem = StockItem.findById(row.stockItemId);
            if (stockItem != null) {
                dto.stockItemName = stockItem.stockItemName;
            }
        }
        if (row.shopItemId != null) {
            Item shopItem = Item.findById(row.shopItemId);
            if (shopItem != null) {
                dto.shopItemName = shopItem.title;
            }
        }
        dto.stockBalance = hmis033bAggregationService.resolveTracerBalance(row);
        if (from != null && to != null) {
            dto.dispensedInPeriod = countDispensedInPeriod(row, from, to);
        }
        return dto;
    }

    private int countDispensedInPeriod(HmisTracerItem tracer, LocalDate from, LocalDate to) {
        if (tracer.stockItemId == null && tracer.shopItemId == null) {
            return 0;
        }
        List<TreatmentRequested> treatments = TreatmentRequested.find(
                "visit.visitDate >= ?1 and visit.visitDate <= ?2", from, to).list();
        if (treatments == null) {
            return 0;
        }
        int count = 0;
        for (TreatmentRequested treatment : treatments) {
            if (matchesTracer(treatment, tracer)) {
                count++;
            }
        }
        return count;
    }

    private boolean matchesTracer(TreatmentRequested treatment, HmisTracerItem tracer) {
        if (treatment == null) {
            return false;
        }
        if (treatment.itemId != null) {
            if (tracer.stockItemId != null && treatment.itemId.equals(tracer.stockItemId)) {
                return true;
            }
            if (tracer.shopItemId != null && treatment.itemId.equals(tracer.shopItemId)) {
                return true;
            }
        }
        if (tracer.stockItemId != null && treatment.stockBatch != null
                && treatment.stockBatch.stockItemId != null
                && treatment.stockBatch.stockItemId.equals(tracer.stockItemId)) {
            return true;
        }
        return false;
    }
}
