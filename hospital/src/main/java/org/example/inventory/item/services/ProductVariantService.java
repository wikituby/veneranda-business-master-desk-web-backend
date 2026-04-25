package org.example.inventory.item.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.*;
import org.example.inventory.item.domain.repositories.*;
import org.example.inventory.item.services.payloads.requests.ProductVariantRequest;
import org.example.inventory.item.services.payloads.responses.dtos.ProductVariantDTO;
import org.example.inventory.item.services.payloads.requests.CompositionRequest;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProductVariantService {

    @Inject
    ProductVariantRepository variantRepository;

    @Inject
    BrandRepository brandRepository;

    @Inject
    ItemRepository itemRepository;

    @Inject
    DosageFormRepository dosageFormRepository;

    @Inject
    FormulationRepository formulationRepository;

    @Transactional
    public Response createVariant(ProductVariantRequest request) {
        // Validate brand
        Brand brand = brandRepository.findById(request.brandId);
        if (brand == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Brand not found for ID: " + request.brandId))
                    .build();
        }

        DosageForm dosageForm = dosageFormRepository.findById(request.dosageFormId);
        if (dosageForm == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("dosage form not found for ID: " + request.dosageFormId))
                    .build();
        }

        Formulation formulation = formulationRepository.findById(request.formulationId);
        if (formulation == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("formulation form not found for ID: " + request.formulationId))
                    .build();
        }

        // Create the variant
        ProductVariant variant = new ProductVariant();
        variant.brandId = brand.id;
        variant.dosageFormId = dosageForm.id;
        variant.formulationId = formulation.id;
        //variant.strengthDescription = request.strengthDescription;

        variantRepository.persist(variant);

        return Response.ok(new ResponseMessage("New product variant added successfully", new ProductVariantDTO(variant))).build();

    }

    public List<ProductVariantDTO> getAllVariants() {
        List<ProductVariant> variants = variantRepository.listAll();
        List<ProductVariantDTO> dtos = new ArrayList<>();
        for (ProductVariant v : variants) {
            dtos.add(new ProductVariantDTO(v));
        }
        return dtos;
    }

    public List<ProductVariantDTO> getVariantsByBrand(Long brandId) {
        List<ProductVariant> variants = variantRepository.find("brand.id", brandId).list();
        List<ProductVariantDTO> dtos = new ArrayList<>();
        for (ProductVariant v : variants) {
            dtos.add(new ProductVariantDTO(v));
        }
        return dtos;
    }

    @Transactional
    public Response getVariantById(Long id) {
        ProductVariant variant = variantRepository.findById(id);
        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Product variant not found for ID: " + id))
                    .build();
        }

        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, new ProductVariantDTO(variant))).build();
    }

    @Transactional
    public Response updateVariant(Long id, ProductVariantRequest request) {
        ProductVariant variant = variantRepository.findById(id);
        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Product variant not found for ID: " + id))
                    .build();
        }

        // Validate brand
        Brand brand = brandRepository.findById(request.brandId);
        if (brand == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Brand not found for ID: " + request.brandId))
                    .build();
        }

        DosageForm dosageForm = dosageFormRepository.findById(request.dosageFormId);
        if (dosageForm == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Dosage form not found for ID: " + request.dosageFormId))
                    .build();
        }

        Formulation formulation = formulationRepository.findById(request.formulationId);
        if (formulation == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Formulation not found for ID: " + request.formulationId))
                    .build();
        }

        variant.brandId = brand.id;
        variant.dosageFormId = dosageForm.id;
        variant.formulationId = formulation.id;

        return Response.ok(new ResponseMessage("Product variant updated successfully", new ProductVariantDTO(variant))).build();
    }

    @Transactional
    public Response deleteVariant(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId);
        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Product variant not found for ID: " + variantId))
                    .build();
        }

        variantRepository.delete(variant);
        return Response.ok(new ResponseMessage("Product variant deleted successfully")).build();
    }
}
