package org.example.cafeteria.inventory.stock.services.responses.dtos;

import org.example.cafeteria.inventory.stock.domains.CanteenStock;
import jakarta.json.bind.annotation.JsonbDateFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CanteenStockDTO {

    public Long id;
    public Long itemId;
    public BigDecimal quantityReceived;
    public BigDecimal unitCostPrice;
    public BigDecimal totalCostPrice;

    public BigDecimal unitSellingPrice;
    public BigDecimal quantityAvailable;
    public BigDecimal newQuantity;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate receiveDate;

    // Constructor to map from Stock entity
    public CanteenStockDTO(CanteenStock canteenStock) {
        this.id = canteenStock.id;
        this.itemId = canteenStock.item != null ? canteenStock.item.id : null;
        this.quantityReceived = canteenStock.quantityReceived;
        this.unitCostPrice = canteenStock.unitCostPrice;
        this.totalCostPrice = canteenStock.totalCostPrice;
        this.unitSellingPrice = canteenStock.unitSellingPrice;
        this.quantityAvailable = canteenStock.quantityAvailable;
        this.newQuantity = canteenStock.newQuantity;
        this.expiryDate = canteenStock.expiryDate;
        this.receiveDate = canteenStock.receiveDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }


    public BigDecimal getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(BigDecimal quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitCostPrice() {
        return unitCostPrice;
    }

    public void setUnitCostPrice(BigDecimal unitCostPrice) {
        this.unitCostPrice = unitCostPrice;
    }


    public BigDecimal getUnitSellingPrice() {
        return unitSellingPrice;
    }

    public void setUnitSellingPrice(BigDecimal unitSellingPrice) {
        this.unitSellingPrice = unitSellingPrice;
    }


    public BigDecimal getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(BigDecimal quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public BigDecimal getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(BigDecimal newQuantity) {
        this.newQuantity = newQuantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(LocalDate receiveDate) {
        this.receiveDate = receiveDate;
    }
}
