package org.example.subscription.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.client.services.PatientGroupCreditUtil;
import org.example.subscription.domains.FacilityBusinessSettings;
import org.example.subscription.domains.repositories.FacilityBusinessSettingsRepository;
import org.example.user.domains.User;

@ApplicationScoped
public class SpecialPrivilegeService {
    @Inject
    FacilityBusinessSettingsRepository businessSettingsRepository;

    public boolean hasPrivilege(User user, Privilege privilege) {
        if (user == null || privilege == null) {
            return false;
        }
        if (this.isPrivilegedRole(user)) {
            return true;
        }
        Long facilityId = user.facilityId;
        if (facilityId == null) {
            return false;
        }
        return this.businessSettingsRepository.findByFacilityId(facilityId).map(settings -> this.userIdsFor((FacilityBusinessSettings)((Object)settings), privilege).contains(user.id)).orElse(false);
    }

    public boolean isPrivilegedRole(User user) {
        if (user == null) {
            return false;
        }
        for (String role : user.allRoleNames()) {
            String n;
            String string = n = role == null ? "" : role.trim().toLowerCase();
            if (!"md".equals(n) && !"admin".equals(n) && !"administrator".equals(n)) continue;
            return true;
        }
        return false;
    }

    public boolean isMdRole(User user) {
        if (user == null) {
            return false;
        }
        for (String role : user.allRoleNames()) {
            String n = role == null ? "" : role.trim().toLowerCase();
            if (!"md".equals(n)) continue;
            return true;
        }
        return false;
    }

    public Set<Long> userIdsFor(FacilityBusinessSettings settings, Privilege privilege) {
        if (settings == null || privilege == null) {
            return Collections.emptySet();
        }
        String raw = switch (privilege) {
            case OPEN_CLOSED_VISIT -> settings.privilegeOpenVisitUserIds;
            case ADD_VENERANDA_MEDICAL_PATIENT -> settings.privilegeVenerandaGroupUserIds;
            case MANAGE_CREDIT_GROUP_RIGHTS -> settings.privilegeCreditGroupUserIds;
        };
        return SpecialPrivilegeService.parseUserIds(raw);
    }

    public Set<Long> creditAllowedGroupIds(Long facilityId) {
        if (facilityId == null) {
            return Collections.emptySet();
        }
        return this.businessSettingsRepository.findByFacilityId(facilityId).map(s -> SpecialPrivilegeService.parseUserIds(s.privilegeCreditAllowedGroupIds)).orElse(Collections.emptySet());
    }

    public boolean groupAllowsCreditDespiteDebt(PatientGroup group, Long facilityId) {
        if (group == null) {
            return false;
        }
        if (PatientGroupCreditUtil.allowsCreditDespiteUnpaidBalance(group)) {
            return true;
        }
        if (group.id == null) {
            return false;
        }
        if (facilityId != null) {
            return this.creditAllowedGroupIds(facilityId).contains(group.id);
        }
        return this.businessSettingsRepository.listAll().stream().anyMatch(s -> SpecialPrivilegeService.parseUserIds(s.privilegeCreditAllowedGroupIds).contains(group.id));
    }

    /** True when any of the patient's groups (primary or memberships) allows credit despite debt. */
    public boolean patientAllowsCreditDespiteDebt(Patient patient, Long facilityId) {
        if (patient == null) {
            return false;
        }
        for (PatientGroup group : patient.resolveAllGroups()) {
            if (groupAllowsCreditDespiteDebt(group, facilityId)) {
                return true;
            }
        }
        return false;
    }

    public boolean requiresCreditGroupAssignPrivilege(PatientGroup group, Long facilityId) {
        if (group == null) {
            return false;
        }
        if (PatientGroupCreditUtil.isVenerandaMedicalGroup(group) || PatientGroupCreditUtil.isLegacyCreditGroupName(group) || Boolean.TRUE.equals(group.allowsCreditDespiteUnpaidBalance)) {
            return true;
        }
        if (group.id == null) {
            return false;
        }
        if (facilityId != null) {
            return this.creditAllowedGroupIds(facilityId).contains(group.id);
        }
        return this.businessSettingsRepository.listAll().stream().anyMatch(s -> SpecialPrivilegeService.parseUserIds(s.privilegeCreditAllowedGroupIds).contains(group.id));
    }

    public static Set<Long> parseUserIds(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(s -> {
            try {
                return Long.parseLong(s);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }).filter(id -> id != null && id > 0L).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static String userIdsToCsv(Iterable<Long> ids) {
        if (ids == null) {
            return null;
        }
        LinkedHashSet<Long> unique = new LinkedHashSet<Long>();
        for (Long id : ids) {
            if (id == null || id <= 0L) continue;
            unique.add(id);
        }
        if (unique.isEmpty()) {
            return null;
        }
        return unique.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static enum Privilege {
        OPEN_CLOSED_VISIT,
        ADD_VENERANDA_MEDICAL_PATIENT,
        MANAGE_CREDIT_GROUP_RIGHTS;

    }
}
