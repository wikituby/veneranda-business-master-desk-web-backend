package org.example.subscription.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="facility_business_settings")
public class FacilityBusinessSettings
extends PanacheEntity {
    @Column(nullable=false, unique=true)
    public Long facilityId;
    @Column(length=2000)
    public String facilityLogoUrl;
    @Column(length=300)
    public String subsidiaryLabel;
    @Column(length=40)
    public String phonePrimary;
    @Column(length=40)
    public String phoneSecondary;
    @Column(length=200)
    public String email;
    @Column(length=120)
    public String financeDepartmentName;
    @Column(length=120)
    public String medicalRecordsDepartmentName;
    @Column(length=120)
    public String diagnosticsDepartmentName;
    @Column(length=200)
    public String bankName;
    @Column(length=80)
    public String ugxAccountNumber;
    @Column(length=80)
    public String usdAccountNumber;
    @Column(length=300)
    public String thankYouMessage;
    @Column(length=500)
    public String invoiceFooterContactLine;
    @Column(nullable=false)
    public Boolean sidebarStacked = true;
    @Column(nullable=false)
    public Boolean otcIncludePrescribing = true;
    @Column(nullable=false)
    public Boolean pharmacyUseStockBatch = true;
    @Column(length=4000)
    public String privilegeOpenVisitUserIds;
    @Column(length=4000)
    public String privilegeVenerandaGroupUserIds;
    @Column(length=4000)
    public String privilegeCreditGroupUserIds;
    @Column(columnDefinition="TEXT")
    public String privilegeCreditAllowedGroupIds;

    /** HMIS reporting metadata (headers on 033b / 105 forms). Stored as TEXT to avoid MySQL row-size limits. */
    @Column(columnDefinition = "TEXT")
    public String hmisFacilityCode;
    @Column(columnDefinition = "TEXT")
    public String hmisFacilityLevel;
    @Column(columnDefinition = "TEXT")
    public String hmisDistrict;
    @Column(columnDefinition = "TEXT")
    public String hmisHsd;
    @Column(columnDefinition = "TEXT")
    public String hmisSubCounty;
    @Column(columnDefinition = "TEXT")
    public String hmisParish;
}
