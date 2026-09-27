package org.example.procedure.procedure.services;

import org.example.procedure.procedure.domains.Procedure;

/** Shared parent-category resolution for lab, dental, MCH department routing. */
public final class ProcedureCategoryUtils {

    private ProcedureCategoryUtils() {
    }

    /** Parent service category: parentCategory, category.parent, or category when it is MCH/maternity. */
    public static String resolveParentCategoryName(Procedure procedure) {
        if (procedure == null) {
            return null;
        }
        if (procedure.parentCategory != null && procedure.parentCategory.name != null
                && !procedure.parentCategory.name.isBlank()) {
            return procedure.parentCategory.name.trim();
        }
        if (procedure.category != null && procedure.category.parent != null
                && procedure.category.parent.name != null
                && !procedure.category.parent.name.isBlank()) {
            return procedure.category.parent.name.trim();
        }
        if (procedure.category != null && procedure.category.name != null
                && isMchOrMaternityParentCategory(procedure.category.name)) {
            return procedure.category.name.trim();
        }
        return null;
    }

    public static boolean isMchOrMaternityParentCategory(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase();
        return normalized.equals("maternity")
                || normalized.equals("mch")
                || normalized.contains("maternity")
                || normalized.contains("mch");
    }
}