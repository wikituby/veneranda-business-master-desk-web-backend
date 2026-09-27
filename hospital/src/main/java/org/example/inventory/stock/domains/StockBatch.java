package org.example.inventory.stock.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class StockBatch extends PanacheEntity {

    @Column
    public String stockItemName;

    @Column
    public Long stockItemId;

    @Column
    public Long storeId; // The store where this batch is located

    @Column
    public String storeName;

    @Column
    public BigDecimal unitCostPrice;

    @Column
    public BigDecimal unitSellingPrice;

    @Column
    public BigDecimal stockAtHand;

    @Column
    public Integer shelfNumber;




        // updated field by the stock batch service
    @Column
    public BigDecimal profitMarginForRetail;

    @Column
    public BigDecimal profitMarginForWholeSale;

    @Column
    public BigDecimal profitMarginForSpecialCase;

    /** Selected default unit selling model for this batch (catalog lives on stock item). */
    @Column
    public Long unitSellingModelId;

    @Column
    public Integer reOrderLevel;
    
    @Column
    public Integer reOrderQuantity;

    @Column
    public Integer reOrderTo;

    //measures 

    @Column
    public String unitOfMeasure;

    @Column
    public BigDecimal lastUnitValue;

    @Column
    public String lastUnitOfMeasure;

    // updated field by the stock batch service


    

    @Column
    public String batchNumber;

    @Column
    public Long stockSupplierId;

    @Column
    public String stockSupplierName;

    @Column
    public String packaging;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime creationDateAndTime;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDateTime upDateDateAndTime;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;
}
