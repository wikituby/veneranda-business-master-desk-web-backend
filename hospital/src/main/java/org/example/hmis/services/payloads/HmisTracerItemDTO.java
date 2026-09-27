package org.example.hmis.services.payloads;

public class HmisTracerItemDTO {
    public Long id;
    public String hmisTracerCode;
    public String tracerName;
    public Long stockItemId;
    public Long shopItemId;
    public String stockItemName;
    public String shopItemName;
    public Boolean active;
    public Integer sortOrder;
    public double stockBalance;
    /** Treatment lines prescribed/dispensed in the requested period (matched by mapped item IDs). */
    public int dispensedInPeriod;

    public static HmisTracerItemDTO from(org.example.hmis.domains.HmisTracerItem row) {
        HmisTracerItemDTO dto = new HmisTracerItemDTO();
        dto.id = row.id;
        dto.hmisTracerCode = row.hmisTracerCode;
        dto.tracerName = row.tracerName;
        dto.stockItemId = row.stockItemId;
        dto.shopItemId = row.shopItemId;
        dto.active = row.active;
        dto.sortOrder = row.sortOrder;
        return dto;
    }
}
