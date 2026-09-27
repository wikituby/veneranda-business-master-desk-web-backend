package org.example.subscription.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.Set;
import org.example.client.domains.PatientGroup;
import org.example.subscription.domains.FacilityBusinessSettings;
import org.example.subscription.domains.HealthFacility;
import org.example.subscription.domains.repositories.FacilityBusinessSettingsRepository;
import org.example.subscription.domains.repositories.FacilitySubscriptionRepository;
import org.example.subscription.domains.repositories.HealthFacilityRepository;
import org.example.subscription.services.FacilityBranding;
import org.example.subscription.services.SpecialPrivilegeService;
import org.example.subscription.services.payloads.BusinessSettingsUpdateRequest;
import org.example.subscription.services.payloads.FacilityBrandingDTO;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

@ApplicationScoped
public class FacilityBrandingService {
    @Inject
    HealthFacilityRepository healthFacilityRepository;
    @Inject
    FacilityBusinessSettingsRepository businessSettingsRepository;
    @Inject
    FacilitySubscriptionRepository facilitySubscriptionRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    SpecialPrivilegeService specialPrivilegeService;

    public FacilityBranding resolveForFacilityId(Long facilityId) {
        HealthFacility facility = facilityId != null ? (HealthFacility)(this.healthFacilityRepository.findById(facilityId)) : null;
        FacilityBusinessSettings settings = facilityId != null ? (FacilityBusinessSettings)this.businessSettingsRepository.findByFacilityId(facilityId).orElse(null) : null;
        return this.buildBranding(facility, settings);
    }

    public FacilityBranding resolveDefaultBranding() {
        Long facilityId = this.resolveDefaultFacilityId();
        if (facilityId == null) {
            return this.defaultBranding();
        }
        return this.resolveForFacilityId(facilityId);
    }

    /** Whether pharmacy dispensing deducts stock batches (true) or shop items (false). */
    public boolean pharmacyUseStockBatchForDefaultFacility() {
        return this.resolvePharmacyUseStockBatch(this.resolveDefaultFacilityId());
    }

    public FacilityBrandingDTO getPublicSettingsDto() {
        Long facilityId = this.resolveDefaultFacilityId();
        if (facilityId == null) {
            FacilityBrandingDTO dto = this.defaultBranding().toDto(null);
            dto.sidebarStacked = true;
            dto.otcIncludePrescribing = true;
            dto.pharmacyUseStockBatch = true;
            return dto;
        }
        FacilityBrandingDTO dto = this.resolveForFacilityId(facilityId).toDto(facilityId);
        this.applySidebarStackedToDto(facilityId, dto);
        this.applyOtcIncludePrescribingToDto(facilityId, dto);
        this.applyPharmacyUseStockBatchToDto(facilityId, dto);
        this.applySpecialPrivilegesToDto(facilityId, dto);
        this.applyHmisMetadataToDto(facilityId, dto);
        return dto;
    }

    public FacilityBrandingDTO getSettingsDtoForUser(Long userId) {
        Long facilityId = this.resolveFacilityIdForUser(userId);
        if (facilityId == null) {
            FacilityBrandingDTO dto = this.defaultBranding().toDto(null);
            dto.sidebarStacked = true;
            dto.otcIncludePrescribing = true;
            dto.pharmacyUseStockBatch = true;
            return dto;
        }
        FacilityBrandingDTO dto = this.resolveForFacilityId(facilityId).toDto(facilityId);
        this.applySidebarStackedToDto(facilityId, dto);
        this.applyOtcIncludePrescribingToDto(facilityId, dto);
        this.applyPharmacyUseStockBatchToDto(facilityId, dto);
        this.applySpecialPrivilegesToDto(facilityId, dto);
        this.applyHmisMetadataToDto(facilityId, dto);
        return dto;
    }

    @Transactional
    public FacilityBrandingDTO updateSettingsForUser(Long userId, BusinessSettingsUpdateRequest request) {
        boolean touchesPrivileges = this.requestTouchesSpecialPrivileges(request);
        if (touchesPrivileges) {
            this.requireMd(userId);
        } else {
            this.requireAdmin(userId);
        }
        Long facilityId = this.resolveFacilityIdForUser(userId);
        if (facilityId == null) {
            facilityId = this.resolveDefaultFacilityId();
        }
        if (facilityId == null) {
            throw new WebApplicationException("No health facility is configured", 400);
        }
        Long resolvedFacilityId = facilityId;
        HealthFacility facility = (HealthFacility)(this.healthFacilityRepository.findById(resolvedFacilityId));
        if (facility == null) {
            throw new WebApplicationException("Facility not found", 404);
        }
        if (request != null) {
            if (request.facilityName != null && !request.facilityName.isBlank()) {
                facility.name = request.facilityName.trim();
            }
            if (request.facilityAddress != null) {
                facility.address = request.facilityAddress.trim();
            }
            this.healthFacilityRepository.persist(facility);
            FacilityBusinessSettings settings = this.businessSettingsRepository.findByFacilityId(resolvedFacilityId).orElseGet(() -> {
                FacilityBusinessSettings created = new FacilityBusinessSettings();
                created.facilityId = resolvedFacilityId;
                created.sidebarStacked = true;
                created.otcIncludePrescribing = true;
                created.pharmacyUseStockBatch = true;
                return created;
            });
            this.applyRequest(settings, request);
            this.businessSettingsRepository.persist(settings);
            if (request.privilegeCreditAllowedGroupIds != null) {
                this.syncCreditAllowedGroups(settings.privilegeCreditAllowedGroupIds);
            }
        }
        FacilityBrandingDTO dto = this.resolveForFacilityId(resolvedFacilityId).toDto(resolvedFacilityId);
        this.applySidebarStackedToDto(resolvedFacilityId, dto);
        this.applyOtcIncludePrescribingToDto(resolvedFacilityId, dto);
        this.applyPharmacyUseStockBatchToDto(resolvedFacilityId, dto);
        this.applySpecialPrivilegesToDto(resolvedFacilityId, dto);
        this.applyHmisMetadataToDto(resolvedFacilityId, dto);
        return dto;
    }

    private void applyRequest(FacilityBusinessSettings settings, BusinessSettingsUpdateRequest request) {
        if (request.facilityLogoUrl != null) {
            settings.facilityLogoUrl = FacilityBrandingService.trimOrNull(request.facilityLogoUrl);
        }
        if (request.subsidiaryLabel != null) {
            settings.subsidiaryLabel = FacilityBrandingService.trimOrNull(request.subsidiaryLabel);
        }
        if (request.phonePrimary != null) {
            settings.phonePrimary = FacilityBrandingService.trimOrNull(request.phonePrimary);
        }
        if (request.phoneSecondary != null) {
            settings.phoneSecondary = FacilityBrandingService.trimOrNull(request.phoneSecondary);
        }
        if (request.email != null) {
            settings.email = FacilityBrandingService.trimOrNull(request.email);
        }
        if (request.financeDepartmentName != null) {
            settings.financeDepartmentName = FacilityBrandingService.trimOrNull(request.financeDepartmentName);
        }
        if (request.medicalRecordsDepartmentName != null) {
            settings.medicalRecordsDepartmentName = FacilityBrandingService.trimOrNull(request.medicalRecordsDepartmentName);
        }
        if (request.diagnosticsDepartmentName != null) {
            settings.diagnosticsDepartmentName = FacilityBrandingService.trimOrNull(request.diagnosticsDepartmentName);
        }
        if (request.bankName != null) {
            settings.bankName = FacilityBrandingService.trimOrNull(request.bankName);
        }
        if (request.ugxAccountNumber != null) {
            settings.ugxAccountNumber = FacilityBrandingService.trimOrNull(request.ugxAccountNumber);
        }
        if (request.usdAccountNumber != null) {
            settings.usdAccountNumber = FacilityBrandingService.trimOrNull(request.usdAccountNumber);
        }
        if (request.thankYouMessage != null) {
            settings.thankYouMessage = FacilityBrandingService.trimOrNull(request.thankYouMessage);
        }
        if (request.invoiceFooterContactLine != null) {
            settings.invoiceFooterContactLine = FacilityBrandingService.trimOrNull(request.invoiceFooterContactLine);
        }
        if (request.sidebarStacked != null) {
            settings.sidebarStacked = request.sidebarStacked;
        }
        if (request.otcIncludePrescribing != null) {
            settings.otcIncludePrescribing = request.otcIncludePrescribing;
        }
        if (request.pharmacyUseStockBatch != null) {
            settings.pharmacyUseStockBatch = request.pharmacyUseStockBatch;
        }
        if (request.privilegeOpenVisitUserIds != null) {
            settings.privilegeOpenVisitUserIds = this.normalizeUserIdCsv(request.privilegeOpenVisitUserIds);
        }
        if (request.privilegeVenerandaGroupUserIds != null) {
            settings.privilegeVenerandaGroupUserIds = this.normalizeUserIdCsv(request.privilegeVenerandaGroupUserIds);
        }
        if (request.privilegeCreditGroupUserIds != null) {
            settings.privilegeCreditGroupUserIds = this.normalizeUserIdCsv(request.privilegeCreditGroupUserIds);
        }
        if (request.privilegeCreditAllowedGroupIds != null) {
            settings.privilegeCreditAllowedGroupIds = this.normalizeUserIdCsv(request.privilegeCreditAllowedGroupIds);
        }
        if (request.hmisFacilityCode != null) {
            settings.hmisFacilityCode = FacilityBrandingService.trimOrNull(request.hmisFacilityCode);
        }
        if (request.hmisFacilityLevel != null) {
            settings.hmisFacilityLevel = FacilityBrandingService.trimOrNull(request.hmisFacilityLevel);
        }
        if (request.hmisDistrict != null) {
            settings.hmisDistrict = FacilityBrandingService.trimOrNull(request.hmisDistrict);
        }
        if (request.hmisHsd != null) {
            settings.hmisHsd = FacilityBrandingService.trimOrNull(request.hmisHsd);
        }
        if (request.hmisSubCounty != null) {
            settings.hmisSubCounty = FacilityBrandingService.trimOrNull(request.hmisSubCounty);
        }
        if (request.hmisParish != null) {
            settings.hmisParish = FacilityBrandingService.trimOrNull(request.hmisParish);
        }
    }

    private boolean requestTouchesSpecialPrivileges(BusinessSettingsUpdateRequest request) {
        if (request == null) {
            return false;
        }
        return request.privilegeOpenVisitUserIds != null || request.privilegeVenerandaGroupUserIds != null || request.privilegeCreditGroupUserIds != null || request.privilegeCreditAllowedGroupIds != null;
    }

    private void syncCreditAllowedGroups(String csv) {
        Set<Long> allowed = SpecialPrivilegeService.parseUserIds(csv);
        for (PatientGroup group : PatientGroup.<PatientGroup>listAll()) {
            if (group == null || group.id == null || !allowed.contains(group.id)) continue;
            group.allowsCreditDespiteUnpaidBalance = true;
            group.persist();
        }
    }

    private String normalizeUserIdCsv(String csv) {
        return SpecialPrivilegeService.userIdsToCsv(SpecialPrivilegeService.parseUserIds(csv));
    }

    private void applySidebarStackedToDto(Long facilityId, FacilityBrandingDTO dto) {
        dto.sidebarStacked = this.resolveSidebarStacked(facilityId);
    }

    private void applyOtcIncludePrescribingToDto(Long facilityId, FacilityBrandingDTO dto) {
        dto.otcIncludePrescribing = this.resolveOtcIncludePrescribing(facilityId);
    }

    private void applyPharmacyUseStockBatchToDto(Long facilityId, FacilityBrandingDTO dto) {
        dto.pharmacyUseStockBatch = this.resolvePharmacyUseStockBatch(facilityId);
    }

    private void applyHmisMetadataToDto(Long facilityId, FacilityBrandingDTO dto) {
        if (facilityId == null || dto == null) {
            return;
        }
        FacilityBusinessSettings settings = this.businessSettingsRepository.findByFacilityId(facilityId).orElse(null);
        if (settings == null) {
            return;
        }
        dto.hmisFacilityCode = settings.hmisFacilityCode;
        dto.hmisFacilityLevel = settings.hmisFacilityLevel;
        dto.hmisDistrict = settings.hmisDistrict;
        dto.hmisHsd = settings.hmisHsd;
        dto.hmisSubCounty = settings.hmisSubCounty;
        dto.hmisParish = settings.hmisParish;
    }

    private void applySpecialPrivilegesToDto(Long facilityId, FacilityBrandingDTO dto) {
        if (facilityId == null) {
            dto.privilegeOpenVisitUserIds = null;
            dto.privilegeVenerandaGroupUserIds = null;
            dto.privilegeCreditGroupUserIds = null;
            dto.privilegeCreditAllowedGroupIds = null;
            return;
        }
        FacilityBusinessSettings settings = this.businessSettingsRepository.findByFacilityId(facilityId).orElse(null);
        if (settings == null) {
            dto.privilegeOpenVisitUserIds = null;
            dto.privilegeVenerandaGroupUserIds = null;
            dto.privilegeCreditGroupUserIds = null;
            dto.privilegeCreditAllowedGroupIds = null;
            return;
        }
        dto.privilegeOpenVisitUserIds = settings.privilegeOpenVisitUserIds;
        dto.privilegeVenerandaGroupUserIds = settings.privilegeVenerandaGroupUserIds;
        dto.privilegeCreditGroupUserIds = settings.privilegeCreditGroupUserIds;
        dto.privilegeCreditAllowedGroupIds = settings.privilegeCreditAllowedGroupIds;
    }

    private boolean resolveOtcIncludePrescribing(Long facilityId) {
        if (facilityId == null) {
            return true;
        }
        return this.businessSettingsRepository.findByFacilityId(facilityId).map(s -> s.otcIncludePrescribing == null || Boolean.TRUE.equals(s.otcIncludePrescribing)).orElse(true);
    }

    private boolean resolvePharmacyUseStockBatch(Long facilityId) {
        if (facilityId == null) {
            return true;
        }
        return this.businessSettingsRepository.findByFacilityId(facilityId).map(s -> s.pharmacyUseStockBatch == null || Boolean.TRUE.equals(s.pharmacyUseStockBatch)).orElse(true);
    }

    private boolean resolveSidebarStacked(Long facilityId) {
        if (facilityId == null) {
            return true;
        }
        return this.businessSettingsRepository.findByFacilityId(facilityId).map(s -> s.sidebarStacked == null || Boolean.TRUE.equals(s.sidebarStacked)).orElse(true);
    }

    private FacilityBranding buildBranding(HealthFacility facility, FacilityBusinessSettings settings) {
        String address;
        String name = facility != null && facility.name != null ? facility.name : "Health Facility";
        String string = address = facility != null && facility.address != null ? facility.address : "";
        if (settings == null) {
            return new FacilityBranding(name, null, address, "", "", "", "", null, null, null, "", "", "", "", "");
        }
        return new FacilityBranding(name, settings.facilityLogoUrl, address, settings.subsidiaryLabel, settings.phonePrimary, settings.phoneSecondary, settings.email, settings.financeDepartmentName, settings.medicalRecordsDepartmentName, settings.diagnosticsDepartmentName, settings.bankName, settings.ugxAccountNumber, settings.usdAccountNumber, settings.thankYouMessage, settings.invoiceFooterContactLine);
    }

    private FacilityBranding defaultBranding() {
        return new FacilityBranding("Health Facility", null, "", "", "", "", "", null, null, null, "", "", "", "", "");
    }

    private Long resolveFacilityIdForUser(Long userId) {
        if (userId == null) {
            return this.resolveDefaultFacilityId();
        }
        User user = (User)(this.userRepository.findById(userId));
        if (user != null && user.facilityId != null) {
            return user.facilityId;
        }
        return this.resolveDefaultFacilityId();
    }

    private Long resolveDefaultFacilityId() {
        return this.facilitySubscriptionRepository.find("order by id desc", new Object[0]).firstResultOptional().map(s -> s.facilityId).orElse(null);
    }

    private void requireAdmin(Long userId) {
        String role;
        User user = userId != null ? (User)(this.userRepository.findById(userId)) : null;
        String string = role = user != null && user.role != null ? user.role.toLowerCase().trim() : "";
        if (!("admin".equals(role) || "md".equals(role) || this.specialPrivilegeService.isPrivilegedRole(user))) {
            throw new WebApplicationException("Only administrators can update business settings", 403);
        }
    }

    private void requireMd(Long userId) {
        User user;
        User user2 = user = userId != null ? (User)(this.userRepository.findById(userId)) : null;
        if (!this.specialPrivilegeService.isMdRole(user)) {
            throw new WebApplicationException("Only the MD can update Special Privileges", 403);
        }
    }

    private static String trimOrNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
