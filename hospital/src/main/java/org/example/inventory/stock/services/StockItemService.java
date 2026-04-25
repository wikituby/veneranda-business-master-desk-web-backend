package org.example.inventory.stock.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.inventory.item.domain.*;
import org.example.inventory.item.domain.repositories.*;
import org.example.inventory.stock.domains.StockBatch;
import org.example.inventory.stock.domains.StockItem;
import org.example.inventory.stock.domains.repositories.StockBatchRepository;
import org.example.inventory.stock.domains.repositories.StockItemRepository;
import org.example.inventory.stock.domains.repositories.StockReceiveRepository;
import org.example.inventory.stock.services.payloads.requests.StockItemIngredientRequest;
import org.example.inventory.stock.services.payloads.requests.StockItemRequest;
import org.example.inventory.stock.services.payloads.responses.dtos.StockItemDTO;
import org.example.inventory.store.domains.repositories.StoreRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class StockItemService {

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    ActiveIngredientRepository activeIngredientRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    BrandRepository brandRepository;
    
    @Inject
    CompositionRepository compositionRepository;
    
    @Inject
    StrengthRepository strengthRepository;
    
    @Inject
    FormulationRepository formulationRepository;
    
    @Inject
    LowestPackageRepository lowestPackageRepository;
    
    @Inject
    RouteOfAdminRepository routeOfAdminRepository;
    
    @Inject
    StrengthUnitRepository strengthUnitRepository;
    
    @Inject
    CategoryRepository categoryRepository;

    @Inject
    StockBatchRepository stockBatchRepository;

    @Inject
    StockReceiveRepository stockReceiveRepository;

    /**
     * Create or update a StockItem
     */
    @Transactional
    public Response createOrUpdateStockItem(StockItemRequest request) {
        try {
            // Generate stockItemName automatically
            String generatedStockItemName = generateStockItemName(request);
            
            // 🔍 Check if item with same generated name and brand exists
            StockItem existing = stockItemRepository.find(
                    "stockItemName = ?1 and brandId = ?2",
                    generatedStockItemName, request.brandId
            ).firstResult();

            if (existing == null) {
                // 🆕 Create new StockItem
                StockItem stockItem = new StockItem();
                stockItem.stockItemName = generatedStockItemName;
                stockItem.brandId = request.brandId;
                
                // Set brandName from brand repository
                if (request.brandId != null) {
                    Brand brand = brandRepository.findById(request.brandId);
                    if (brand != null && brand.name != null) {
                        stockItem.brandName = brand.name;
                    }
                }
                
                stockItem.formulationId = request.formulationId;
                // Set formulationName from formulation repository
                if (request.formulationId != null) {
                    Formulation formulation = formulationRepository.findById(request.formulationId);
                    if (formulation != null && formulation.name != null) {
                        stockItem.formulationName = formulation.name;
                    }
                }
                
                stockItem.lowestPackageId = request.lowestPackageId;
                // Set lowestPackageTitle from lowestPackage repository
                if (request.lowestPackageId != null) {
                    LowestPackage lowestPackage = lowestPackageRepository.findById(request.lowestPackageId);
                    if (lowestPackage != null && lowestPackage.title != null) {
                        stockItem.lowestPackageTitle = lowestPackage.title;
                    }
                }
                
                stockItem.totalCompositionValue = request.totalCompositionValue;
                stockItem.totalCompositionUnitId = request.totalCompositionUnitId;
                // Set totalCompositionUnitTitle from strengthUnit repository
                if (request.totalCompositionUnitId != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.totalCompositionUnitId);
                    if (unit != null && unit.title != null) {
                        stockItem.totalCompositionUnitTitle = unit.title;
                    }
                }
                
                stockItem.reconstitutionValue = request.reconstitutionValue;
                stockItem.reconstitutionUnitId = request.reconstitutionUnitId;
                // Set reconstitutionUnitTitle from strengthUnit repository
                if (request.reconstitutionUnitId != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.reconstitutionUnitId);
                    if (unit != null && unit.title != null) {
                        stockItem.reconstitutionUnitTitle = unit.title;
                    }
                }
                
                stockItem.itemCategoryId = request.itemCategoryId;
                // Set itemCategoryName from category repository
                if (request.itemCategoryId != null) {
                    Category category = categoryRepository.findById(request.itemCategoryId);
                    if (category != null && category.name != null) {
                        stockItem.itemCategoryName = category.name;
                    }
                }
                
                stockItem.routeOfAdminId = request.routeOfAdminId;
                // Set routeOfAdminTitle from routeOfAdmin repository
                if (request.routeOfAdminId != null) {
                    RouteOfAdmin routeOfAdmin = routeOfAdminRepository.findById(request.routeOfAdminId);
                    if (routeOfAdmin != null && routeOfAdmin.title != null) {
                        stockItem.routeOfAdminTitle = routeOfAdmin.title;
                    }
                }
                
                // Updated fields by the stock batch service
                stockItem.lastUnitOfSellMeasure = request.lastUnitOfSellMeasure;
                stockItem.lastUnitOfSellMeasureStrength = request.lastUnitOfSellMeasureStrength;
                stockItem.lastUnitOfSellMeasureStrengthUnit = request.lastUnitOfSellMeasureStrengthUnit;
                // Set lastUnitOfSellMeasureStrengthUnitTitle from strengthUnit repository
                if (request.lastUnitOfSellMeasureStrengthUnit != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.lastUnitOfSellMeasureStrengthUnit);
                    if (unit != null && unit.title != null) {
                        stockItem.lastUnitOfSellMeasureStrengthUnitTitle = unit.title;
                    }
                }
                
                // Prescribing units
                stockItem.lastUnitOfPrescribingMeasureStrengthUnit = request.lastUnitOfPrescribingMeasureStrengthUnit;
                // Set lastUnitOfPrescribingMeasureStrengthUnitTitle from strengthUnit repository
                if (request.lastUnitOfPrescribingMeasureStrengthUnit != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.lastUnitOfPrescribingMeasureStrengthUnit);
                    if (unit != null && unit.title != null) {
                        stockItem.lastUnitOfPrescribingMeasureStrengthUnitTitle = unit.title;
                    }
                }
                
                // Others - medical information fields
                stockItem.indication = request.indication;
                stockItem.image = request.image;
                stockItem.contraIndication = request.contraIndication;
                stockItem.drugIteractions = request.drugIteractions;
                stockItem.description = request.description;
                stockItem.pharmacodynamics = request.pharmacodynamics;
                stockItem.pharmacokinetics = request.pharmacokinetics;
                stockItem.adverseEffects = request.adverseEffects;
                stockItem.dosage = request.dosage;
                stockItem.notes = request.notes;
                stockItem.methodOfReconstitution = request.methodOfReconstitution;
                stockItem.descriptionBeforeReconstitution = request.descriptionBeforeReconstitution;
                stockItem.descriptionAfterReconstitution = request.descriptionAfterReconstitution;
                stockItem.storage = request.storage;
                stockItem.antiDote = request.antiDote;
                
                stockItem.creationDate = LocalDate.now();
                stockItem.updateDate = LocalDate.now();

                stockItem.persist();
                
                // Save ingredients/compositions
                saveCompositions(stockItem.id, request.ingredients);
                
                return Response.ok(new ResponseMessage("Stock item created successfully", new StockItemDTO(stockItem))).build();
            } else {
                // 🔄 Update existing StockItem
                existing.stockItemName = generatedStockItemName;
                existing.brandId = request.brandId;
                
                // Set brandName from brand repository
                if (request.brandId != null) {
                    Brand brand = brandRepository.findById(request.brandId);
                    if (brand != null && brand.name != null) {
                        existing.brandName = brand.name;
                    }
                }

                existing.formulationId = request.formulationId;
                // Set formulationName from formulation repository
                if (request.formulationId != null) {
                    Formulation formulation = formulationRepository.findById(request.formulationId);
                    if (formulation != null && formulation.name != null) {
                        existing.formulationName = formulation.name;
                    }
                }
                
                existing.lowestPackageId = request.lowestPackageId;
                // Set lowestPackageTitle from lowestPackage repository
                if (request.lowestPackageId != null) {
                    LowestPackage lowestPackage = lowestPackageRepository.findById(request.lowestPackageId);
                    if (lowestPackage != null && lowestPackage.title != null) {
                        existing.lowestPackageTitle = lowestPackage.title;
                    }
                }
                
                existing.totalCompositionValue = request.totalCompositionValue;
                existing.totalCompositionUnitId = request.totalCompositionUnitId;
                // Set totalCompositionUnitTitle from strengthUnit repository
                if (request.totalCompositionUnitId != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.totalCompositionUnitId);
                    if (unit != null && unit.title != null) {
                        existing.totalCompositionUnitTitle = unit.title;
                    }
                }
                
                existing.reconstitutionValue = request.reconstitutionValue;
                existing.reconstitutionUnitId = request.reconstitutionUnitId;
                // Set reconstitutionUnitTitle from strengthUnit repository
                if (request.reconstitutionUnitId != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.reconstitutionUnitId);
                    if (unit != null && unit.title != null) {
                        existing.reconstitutionUnitTitle = unit.title;
                    }
                }
                
                existing.itemCategoryId = request.itemCategoryId;
                // Set itemCategoryName from category repository
                if (request.itemCategoryId != null) {
                    Category category = categoryRepository.findById(request.itemCategoryId);
                    if (category != null && category.name != null) {
                        existing.itemCategoryName = category.name;
                    }
                }
                
                existing.routeOfAdminId = request.routeOfAdminId;
                // Set routeOfAdminTitle from routeOfAdmin repository
                if (request.routeOfAdminId != null) {
                    RouteOfAdmin routeOfAdmin = routeOfAdminRepository.findById(request.routeOfAdminId);
                    if (routeOfAdmin != null && routeOfAdmin.title != null) {
                        existing.routeOfAdminTitle = routeOfAdmin.title;
                    }
                }
                
                // Updated fields by the stock batch service
                existing.lastUnitOfSellMeasure = request.lastUnitOfSellMeasure;
                existing.lastUnitOfSellMeasureStrength = request.lastUnitOfSellMeasureStrength;
                existing.lastUnitOfSellMeasureStrengthUnit = request.lastUnitOfSellMeasureStrengthUnit;
                // Set lastUnitOfSellMeasureStrengthUnitTitle from strengthUnit repository
                if (request.lastUnitOfSellMeasureStrengthUnit != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.lastUnitOfSellMeasureStrengthUnit);
                    if (unit != null && unit.title != null) {
                        existing.lastUnitOfSellMeasureStrengthUnitTitle = unit.title;
                    }
                }
                
                // Prescribing units
                existing.lastUnitOfPrescribingMeasureStrengthUnit = request.lastUnitOfPrescribingMeasureStrengthUnit;
                // Set lastUnitOfPrescribingMeasureStrengthUnitTitle from strengthUnit repository
                if (request.lastUnitOfPrescribingMeasureStrengthUnit != null) {
                    StrengthUnit unit = strengthUnitRepository.findById(request.lastUnitOfPrescribingMeasureStrengthUnit);
                    if (unit != null && unit.title != null) {
                        existing.lastUnitOfPrescribingMeasureStrengthUnitTitle = unit.title;
                    }
                }
                
                // Others - medical information fields
                existing.indication = request.indication;
                existing.image = request.image;
                existing.contraIndication = request.contraIndication;
                existing.drugIteractions = request.drugIteractions;
                existing.description = request.description;
                existing.pharmacodynamics = request.pharmacodynamics;
                existing.pharmacokinetics = request.pharmacokinetics;
                existing.adverseEffects = request.adverseEffects;
                existing.dosage = request.dosage;
                existing.notes = request.notes;
                existing.methodOfReconstitution = request.methodOfReconstitution;
                existing.descriptionBeforeReconstitution = request.descriptionBeforeReconstitution;
                existing.descriptionAfterReconstitution = request.descriptionAfterReconstitution;
                existing.storage = request.storage;
                existing.antiDote = request.antiDote;
                
                existing.updateDate = LocalDate.now();

                stockItemRepository.persist(existing);
                syncStockItemNameToBatchesAndReceives(existing.id, existing.stockItemName);
                
                // Delete existing compositions and save new ones
                deleteCompositionsByStockItemId(existing.id);
                saveCompositions(existing.id, request.ingredients);
                
                return Response.ok(new ResponseMessage("Stock item updated successfully", new StockItemDTO(existing))).build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage("Error while saving StockItem: " + e.getMessage(), null))
                    .build();
        }
    }

    /**
     * List all StockItems
     */
    @Transactional
    public List<StockItemDTO> getAllStockItems() {
        return stockItemRepository.listAll(Sort.descending("id"))
                .stream()
                .map(StockItemDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Find StockItem by ID
     */
    @Transactional
    public Response getStockItemById(Long id) {
        StockItem stockItem = stockItemRepository.findById(id);
        if (stockItem == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("StockItem not found with ID: " + id, null))
                    .build();
        }
        return Response.ok(new ResponseMessage("StockItem found", new StockItemDTO(stockItem))).build();
    }

    /**
     * Update StockItem by ID
     */
    @Transactional
    public Response updateStockItem(Long id, StockItemRequest request) {
        try {
            StockItem stockItem = stockItemRepository.findById(id);
            if (stockItem == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("StockItem not found with ID: " + id, null))
                        .build();
            }

            // Generate stockItemName automatically
            String generatedStockItemName = generateStockItemName(request);
            stockItem.stockItemName = generatedStockItemName;
            stockItem.brandId = request.brandId;
            
            // Set brandName from brand repository
            if (request.brandId != null) {
                Brand brand = brandRepository.findById(request.brandId);
                if (brand != null && brand.name != null) {
                    stockItem.brandName = brand.name;
                }
            }

            stockItem.formulationId = request.formulationId;
            // Set formulationName from formulation repository
            if (request.formulationId != null) {
                Formulation formulation = formulationRepository.findById(request.formulationId);
                if (formulation != null && formulation.name != null) {
                    stockItem.formulationName = formulation.name;
                }
            }
            
            stockItem.lowestPackageId = request.lowestPackageId;
            // Set lowestPackageTitle from lowestPackage repository
            if (request.lowestPackageId != null) {
                LowestPackage lowestPackage = lowestPackageRepository.findById(request.lowestPackageId);
                if (lowestPackage != null && lowestPackage.title != null) {
                    stockItem.lowestPackageTitle = lowestPackage.title;
                }
            }
            
            stockItem.totalCompositionValue = request.totalCompositionValue;
            stockItem.totalCompositionUnitId = request.totalCompositionUnitId;
            // Set totalCompositionUnitTitle from strengthUnit repository
            if (request.totalCompositionUnitId != null) {
                StrengthUnit unit = strengthUnitRepository.findById(request.totalCompositionUnitId);
                if (unit != null && unit.title != null) {
                    stockItem.totalCompositionUnitTitle = unit.title;
                }
            }
            
            stockItem.reconstitutionValue = request.reconstitutionValue;
            stockItem.reconstitutionUnitId = request.reconstitutionUnitId;
            // Set reconstitutionUnitTitle from strengthUnit repository
            if (request.reconstitutionUnitId != null) {
                StrengthUnit unit = strengthUnitRepository.findById(request.reconstitutionUnitId);
                if (unit != null && unit.title != null) {
                    stockItem.reconstitutionUnitTitle = unit.title;
                }
            }
            
            stockItem.itemCategoryId = request.itemCategoryId;
            // Set itemCategoryName from category repository
            if (request.itemCategoryId != null) {
                Category category = categoryRepository.findById(request.itemCategoryId);
                if (category != null && category.name != null) {
                    stockItem.itemCategoryName = category.name;
                }
            }
            
            stockItem.routeOfAdminId = request.routeOfAdminId;
            // Set routeOfAdminTitle from routeOfAdmin repository
            if (request.routeOfAdminId != null) {
                RouteOfAdmin routeOfAdmin = routeOfAdminRepository.findById(request.routeOfAdminId);
                if (routeOfAdmin != null && routeOfAdmin.title != null) {
                    stockItem.routeOfAdminTitle = routeOfAdmin.title;
                }
            }
            
            // Updated fields by the stock batch service
            stockItem.lastUnitOfSellMeasure = request.lastUnitOfSellMeasure;
            stockItem.lastUnitOfSellMeasureStrength = request.lastUnitOfSellMeasureStrength;
            stockItem.lastUnitOfSellMeasureStrengthUnit = request.lastUnitOfSellMeasureStrengthUnit;
            // Set lastUnitOfSellMeasureStrengthUnitTitle from strengthUnit repository
            if (request.lastUnitOfSellMeasureStrengthUnit != null) {
                StrengthUnit unit = strengthUnitRepository.findById(request.lastUnitOfSellMeasureStrengthUnit);
                if (unit != null && unit.title != null) {
                    stockItem.lastUnitOfSellMeasureStrengthUnitTitle = unit.title;
                }
            }
            
            // Prescribing units
            stockItem.lastUnitOfPrescribingMeasureStrengthUnit = request.lastUnitOfPrescribingMeasureStrengthUnit;
            // Set lastUnitOfPrescribingMeasureStrengthUnitTitle from strengthUnit repository
            if (request.lastUnitOfPrescribingMeasureStrengthUnit != null) {
                StrengthUnit unit = strengthUnitRepository.findById(request.lastUnitOfPrescribingMeasureStrengthUnit);
                if (unit != null && unit.title != null) {
                    stockItem.lastUnitOfPrescribingMeasureStrengthUnitTitle = unit.title;
                }
            }
            
            // Others - medical information fields
            stockItem.indication = request.indication;
            stockItem.image = request.image;
            stockItem.contraIndication = request.contraIndication;
            stockItem.drugIteractions = request.drugIteractions;
            stockItem.description = request.description;
            stockItem.pharmacodynamics = request.pharmacodynamics;
            stockItem.pharmacokinetics = request.pharmacokinetics;
            stockItem.adverseEffects = request.adverseEffects;
            stockItem.dosage = request.dosage;
            stockItem.notes = request.notes;
            stockItem.methodOfReconstitution = request.methodOfReconstitution;
            stockItem.descriptionBeforeReconstitution = request.descriptionBeforeReconstitution;
            stockItem.descriptionAfterReconstitution = request.descriptionAfterReconstitution;
            stockItem.storage = request.storage;
            stockItem.antiDote = request.antiDote;
            
            stockItem.updateDate = LocalDate.now();

            stockItemRepository.persist(stockItem);
            syncStockItemNameToBatchesAndReceives(stockItem.id, stockItem.stockItemName);
            
            // Delete existing compositions and save new ones
            deleteCompositionsByStockItemId(stockItem.id);
            saveCompositions(stockItem.id, request.ingredients);
            
            return Response.ok(new ResponseMessage("Stock item updated successfully", new StockItemDTO(stockItem))).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage("Error while updating StockItem: " + e.getMessage(), null))
                    .build();
        }
    }

    /**
     * Delete StockItem by ID
     */
    @Transactional
    public Response deleteStockItem(Long id) {
        boolean deleted = stockItemRepository.deleteById(id);
        if (deleted) {
            return Response.ok(new ResponseMessage("StockItem deleted successfully")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("StockItem not found with ID: " + id, null))
                    .build();
        }
    }

    /**
     * Update missing names and titles for existing StockItems
     * This method populates name/title fields for StockItems that have IDs but missing names/titles
     */
    @Transactional
    public Response updateMissingNamesAndTitles() {
        try {
            List<StockItem> allStockItems = stockItemRepository.listAll();
            int updatedCount = 0;
            int totalCount = allStockItems.size();

            for (StockItem stockItem : allStockItems) {
                boolean wasUpdated = false;

                // Update brandName
                if (stockItem.brandId != null && (stockItem.brandName == null || stockItem.brandName.trim().isEmpty())) {
                    Brand brand = brandRepository.findById(stockItem.brandId);
                    if (brand != null && brand.name != null) {
                        stockItem.brandName = brand.name;
                        wasUpdated = true;
                    }
                }

                // Update formulationName
                if (stockItem.formulationId != null && (stockItem.formulationName == null || stockItem.formulationName.trim().isEmpty())) {
                    Formulation formulation = formulationRepository.findById(stockItem.formulationId);
                    if (formulation != null && formulation.name != null) {
                        stockItem.formulationName = formulation.name;
                        wasUpdated = true;
                    }
                }

                // Update lowestPackageTitle
                if (stockItem.lowestPackageId != null && (stockItem.lowestPackageTitle == null || stockItem.lowestPackageTitle.trim().isEmpty())) {
                    LowestPackage lowestPackage = lowestPackageRepository.findById(stockItem.lowestPackageId);
                    if (lowestPackage != null && lowestPackage.title != null) {
                        stockItem.lowestPackageTitle = lowestPackage.title;
                        wasUpdated = true;
                    }
                }

                // Update totalCompositionUnitTitle
                if (stockItem.totalCompositionUnitId != null && (stockItem.totalCompositionUnitTitle == null || stockItem.totalCompositionUnitTitle.trim().isEmpty())) {
                    StrengthUnit unit = strengthUnitRepository.findById(stockItem.totalCompositionUnitId);
                    if (unit != null && unit.title != null) {
                        stockItem.totalCompositionUnitTitle = unit.title;
                        wasUpdated = true;
                    }
                }

                // Update reconstitutionUnitTitle
                if (stockItem.reconstitutionUnitId != null && (stockItem.reconstitutionUnitTitle == null || stockItem.reconstitutionUnitTitle.trim().isEmpty())) {
                    StrengthUnit unit = strengthUnitRepository.findById(stockItem.reconstitutionUnitId);
                    if (unit != null && unit.title != null) {
                        stockItem.reconstitutionUnitTitle = unit.title;
                        wasUpdated = true;
                    }
                }

                // Update itemCategoryName
                if (stockItem.itemCategoryId != null && (stockItem.itemCategoryName == null || stockItem.itemCategoryName.trim().isEmpty())) {
                    Category category = categoryRepository.findById(stockItem.itemCategoryId);
                    if (category != null && category.name != null) {
                        stockItem.itemCategoryName = category.name;
                        wasUpdated = true;
                    }
                }

                // Update routeOfAdminTitle
                if (stockItem.routeOfAdminId != null && (stockItem.routeOfAdminTitle == null || stockItem.routeOfAdminTitle.trim().isEmpty())) {
                    RouteOfAdmin routeOfAdmin = routeOfAdminRepository.findById(stockItem.routeOfAdminId);
                    if (routeOfAdmin != null && routeOfAdmin.title != null) {
                        stockItem.routeOfAdminTitle = routeOfAdmin.title;
                        wasUpdated = true;
                    }
                }

                if (wasUpdated) {
                    stockItem.updateDate = LocalDate.now();
                    stockItemRepository.persist(stockItem);
                    updatedCount++;
                }
            }

            String message = String.format("Update completed. %d out of %d stock items were updated with missing names/titles.", updatedCount, totalCount);
            return Response.ok(new ResponseMessage(message, null)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage("Error while updating StockItem names/titles: " + e.getMessage(), null))
                    .build();
        }
    }
    
    /**
     * Save compositions for a StockItem
     * Creates Strength records and links them via Composition records
     */
    private void saveCompositions(Long stockItemId, List<StockItemIngredientRequest> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        
        for (StockItemIngredientRequest ingredient : ingredients) {
            Long activeIngredientPk = resolveActiveIngredientId(ingredient);
            if (activeIngredientPk == null || ingredient.strength == null || ingredient.unitId == null) {
                continue; // Skip invalid ingredients
            }
            
            // Create or find existing Strength record
            Strength strength = strengthRepository.find(
                "activeIngredientId = ?1 and strengthValue = ?2 and strengthUnitId = ?3",
                activeIngredientPk, ingredient.strength, ingredient.unitId
            ).firstResult();
            
            if (strength == null) {
                // Create new Strength
                strength = new Strength();
                strength.activeIngredientId = activeIngredientPk;
                strength.strengthValue = ingredient.strength;
                strength.strengthUnitId = ingredient.unitId;
                strengthRepository.persist(strength);
            }
            
            // Create Composition linking StockItem to Strength
            Composition composition = new Composition();
            composition.stockItemId = stockItemId;
            composition.strengthId = strength.id;
            compositionRepository.persist(composition);
        }
    }
    
    /**
     * Delete all compositions for a StockItem
     */
    private void deleteCompositionsByStockItemId(Long stockItemId) {
        List<Composition> compositions = compositionRepository.find("stockItemId", stockItemId).list();
        for (Composition composition : compositions) {
            compositionRepository.delete(composition);
        }
    }

    /**
     * Keep denormalized stock item names in sync after rename.
     */
    private void syncStockItemNameToBatchesAndReceives(Long stockItemId, String stockItemName) {
        if (stockItemId == null || stockItemName == null) {
            return;
        }

        List<StockBatch> relatedBatches = stockBatchRepository.find("stockItemId", stockItemId).list();
        for (StockBatch batch : relatedBatches) {
            batch.stockItemName = stockItemName;
            stockReceiveRepository.update(
                    "stockItemName = ?1 where stockBatchId = ?2",
                    stockItemName,
                    batch.id
            );
        }
    }
    
    /**
     * Generate stockItemName from: Brand + Formulation + LowestPackage + RouteOfAdmin + TotalComposition + TotalCompositionUnit + ActiveIngredients
     */
    private String generateStockItemName(StockItemRequest request) {
        List<String> parts = new java.util.ArrayList<>();
        
        // Brand name
        if (request.brandId != null) {
            Brand brand = brandRepository.findById(request.brandId);
            if (brand != null && brand.name != null) {
                parts.add(brand.name);
            }
        }
        
        // Formulation name
        if (request.formulationId != null) {
            Formulation formulation = formulationRepository.findById(request.formulationId);
            if (formulation != null && formulation.name != null) {
                parts.add(formulation.name);
            }
        }
        
        // Lowest Package title
        if (request.lowestPackageId != null) {
            LowestPackage lowestPackage = lowestPackageRepository.findById(request.lowestPackageId);
            if (lowestPackage != null && lowestPackage.title != null) {
                parts.add(lowestPackage.title);
            }
        }
        
        // Route of Admin title
        if (request.routeOfAdminId != null) {
            RouteOfAdmin routeOfAdmin = routeOfAdminRepository.findById(request.routeOfAdminId);
            if (routeOfAdmin != null && routeOfAdmin.title != null) {
                parts.add(routeOfAdmin.title);
            }
        }
        
        // Total Composition Value + Unit (using abbreviation)
        if (request.totalCompositionValue != null && request.totalCompositionUnitId != null) {
            StrengthUnit unit = strengthUnitRepository.findById(request.totalCompositionUnitId);
            String unitName = "";
            if (unit != null) {
                // Use abbreviation if available, otherwise fall back to title
                if (unit.standardAbbreviation != null && !unit.standardAbbreviation.trim().isEmpty()) {
                    unitName = unit.standardAbbreviation;
                } else if (unit.title != null) {
                    unitName = unit.title;
                }
            }
            parts.add(request.totalCompositionValue + " " + unitName);
        }
        
        // Reconstitution Value + Unit (using abbreviation)
        if (request.reconstitutionValue != null && request.reconstitutionUnitId != null) {
            StrengthUnit unit = strengthUnitRepository.findById(request.reconstitutionUnitId);
            String unitName = "";
            if (unit != null) {
                // Use abbreviation if available, otherwise fall back to title
                if (unit.standardAbbreviation != null && !unit.standardAbbreviation.trim().isEmpty()) {
                    unitName = unit.standardAbbreviation;
                } else if (unit.title != null) {
                    unitName = unit.title;
                }
            }
            parts.add(request.reconstitutionValue + " " + unitName);
        }
        
        // Active Ingredients with their strengths (using abbreviation)
        if (request.ingredients != null && !request.ingredients.isEmpty()) {
            for (StockItemIngredientRequest ingredient : request.ingredients) {
                Long activeIngredientPk = resolveActiveIngredientId(ingredient);
                if (activeIngredientPk != null && ingredient.strength != null && ingredient.unitId != null) {
                    ActiveIngredient activeIngredient = activeIngredientRepository.findById(activeIngredientPk);
                    StrengthUnit unit = strengthUnitRepository.findById(ingredient.unitId);
                    String itemName = (activeIngredient != null && activeIngredient.genericName != null) ? activeIngredient.genericName : "";
                    String unitName = "";
                    if (unit != null) {
                        // Use abbreviation if available, otherwise fall back to title
                        if (unit.standardAbbreviation != null && !unit.standardAbbreviation.trim().isEmpty()) {
                            unitName = unit.standardAbbreviation;
                        } else if (unit.title != null) {
                            unitName = unit.title;
                        }
                    }
                    parts.add(itemName + " " + ingredient.strength + " " + unitName);
                }
            }
        }
        
        return String.join(", ", parts);
    }

    private static Long resolveActiveIngredientId(StockItemIngredientRequest ingredient) {
        if (ingredient == null) {
            return null;
        }
        if (ingredient.activeIngredientId != null) {
            return ingredient.activeIngredientId;
        }
        return ingredient.itemId;
    }
}
