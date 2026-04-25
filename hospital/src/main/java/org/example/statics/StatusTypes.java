package org.example.statics;

public enum StatusTypes {

    ACCEPTED("Accepted".toUpperCase()),
    ACTIVE("Active".toUpperCase()),
    APPROVED("Approved".toUpperCase()),
    ALLOWED_CREDIT("Allowed on Credit".toUpperCase()),
    BILLED("Billed".toUpperCase()),
    UPDATED_SUCCESSFULLY("Updated Successfully".toUpperCase()),
    CREATED("Created".toUpperCase()),
    VISIT_CREATED_SUCCESSFULLY("Visit Created successfully".toUpperCase()),
    REQUESTED("Requested Successfully".toUpperCase()),
    PATIENT_CREATED_SUCCESSFULLY("Patient Created successfully".toUpperCase()),
    CLOSED("Closed".toUpperCase()),
    CLOSED_AND_APPROVED("Closed and Approved".toUpperCase()),
    VISIT_CLOSED("Visit Closed".toUpperCase()),
    COMPLETED("Completed".toUpperCase()),
    DEACTIVATED("De-activated".toUpperCase()),
    DECLINED("Declined".toUpperCase()),
    DELETED("Deleted".toUpperCase()),
    DELIVERED("Delivered".toUpperCase()),
    DRAFT("Draft".toUpperCase()),
    INACTIVE("In-active".toUpperCase()),
    ISSUED("Issued".toUpperCase()),
    INVOICED("Invoiced".toUpperCase()),
    NOT_BILLED("Not Billed".toUpperCase()),
    NOT_DELIVERED("Not Delivered".toUpperCase()),
    NOT_INVOICED("Not Invoiced".toUpperCase()),
    NOT_RECEIVED("Not Received".toUpperCase()),
    OPEN("Open".toUpperCase()),
    PAID("Paid".toUpperCase()),
    PAID_PARTIALLY("Paid Partially".toUpperCase()),
    PENDING("Pending".toUpperCase()),
    PENDING_PAYMENT("Awaiting Payment".toUpperCase()),
    PUBLISHED("Published".toUpperCase()),
    RECEIVED("Received".toUpperCase()),
    RECEIVED_PARTIALLY("Received Partially".toUpperCase()),
    REJECTED("Rejected".toUpperCase()),
    REVERSED("Reversed".toUpperCase()),
    RUNNING("Running".toUpperCase()),
    SENT("Sent".toUpperCase()),
    CONFLICT("Already exists".toUpperCase()),
    TRANSFERRED("Transferred".toUpperCase());

    public final String label;

    StatusTypes(String label) {

        this.label = label;
    }

    public static StatusTypes getEnum(String label) {

        StatusTypes gotten = null;
        for (StatusTypes eenum : StatusTypes.values()) {
            if (eenum.label.equals(label)) {
                gotten = eenum;
            }
        }
        return gotten;
    }
}






