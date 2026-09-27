package org.example.client.services;

import org.example.client.domains.PatientGroup;

public final class PatientGroupCreditUtil {
    private PatientGroupCreditUtil() {
    }

    public static boolean allowsCreditDespiteUnpaidBalance(PatientGroup group) {
        if (group == null) {
            return false;
        }
        if (Boolean.TRUE.equals(group.allowsCreditDespiteUnpaidBalance)) {
            return true;
        }
        return PatientGroupCreditUtil.isLegacyCreditGroupName(group);
    }

    public static boolean isLegacyCreditGroupName(PatientGroup group) {
        if (group == null || group.groupName == null) {
            return false;
        }
        String name = group.groupName.trim();
        return "veneranda medical".equalsIgnoreCase(name) || "katoma child youth development centre - UG1005".equalsIgnoreCase(name);
    }

    public static boolean isVenerandaMedicalGroup(PatientGroup group) {
        if (group == null || group.groupName == null) {
            return false;
        }
        return "veneranda medical".equalsIgnoreCase(group.groupName.trim());
    }
}
