package org.example.inventory.item.services.payloads.responses.dtos;

import org.example.inventory.item.domain.ProductVariant;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDTO {
    public Long id;
    public Long brandId;
    public Long dosageFormId;
    public Long formulationId;

    public List<CompositionDTO> compositions = new ArrayList<>();

    public ProductVariantDTO(ProductVariant variant) {
        this.id = variant.id;
        this.brandId = variant.brandId;
        this.dosageFormId = variant.dosageFormId;
        this.formulationId = variant.formulationId;


    }
}
