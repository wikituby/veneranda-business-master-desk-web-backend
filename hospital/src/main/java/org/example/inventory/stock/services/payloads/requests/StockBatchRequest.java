package org.example.inventory.stock.services.payloads.requests;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockBatchRequest {

    //public String stockItemName;
    public Long stockItemId;
    public Long storeId;
    public BigDecimal unitCostPrice;
    public BigDecimal totalCostPrice;
    public BigDecimal unitSellingPrice;
    //public BigDecimal stockAtHand;
    public Integer reOrderLevel;
    public Integer reOrderTo;
    public Integer reOrderQuantity;
    public String unitOfMeasure;
    public BigDecimal lastUnitValue;
    public String lastUnitOfMeasure;
    public String batchNumber;
    public LocalDate receiveDate;
    public LocalDate expiryDate;
    //public Integer shelfNumber;

    public String packaging;
    public String invoiceNumber;


    public Long stockSupplierId;

    public Long purchaseOrderId;
    public String purchaseOrderNo;
  
    public BigDecimal quantityReceived;

    public BigDecimal profitMarginForRetail;
    public BigDecimal profitMarginForWholeSale;
    public BigDecimal profitMarginForSpecialCase;

    /** Unit selling model applied on this receive (price taken from model). */
    public Long unitSellingModelId;

}
