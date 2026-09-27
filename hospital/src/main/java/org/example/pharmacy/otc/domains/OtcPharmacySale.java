package org.example.pharmacy.otc.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "otc_pharmacy_sale")
public class OtcPharmacySale extends PanacheEntity {

    @Column(nullable = false)
    public Integer salePlainNo;

    @Column(nullable = false)
    public String saleNo;

    @Column(nullable = false)
    public LocalDate saleDate;

    @Column(nullable = false)
    public LocalTime saleTime;

    @Column(nullable = false)
    public BigDecimal totalAmount;

    @Column(nullable = false)
    public BigDecimal amountReceived;

    @Column(nullable = false)
    public BigDecimal changeAmount;

    @Column(nullable = false)
    public String paymentForm;

    @Column
    public String receivedBy;

    @Column
    public String notes;

    /** Set when completing a visit prescription sale at the pharmacy counter. */
    @Column
    public Long visitId;

    /** OTC walk-in client (patient) tagged on the sale. */
    @Column
    public Long patientId;

    @Column(length = 200)
    public String patientName;

    /**
     * Commercial document stage for this transaction:
     * quotation | sales_order | proforma | sales_invoice | receipt
     */
    @Column(length = 40)
    public String documentType;

    /** When false, the order is deactivated (hidden from normal POS use). */
    @Column(nullable = false)
    public boolean active = true;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OtcPharmacySaleLine> lines = new ArrayList<>();
}
