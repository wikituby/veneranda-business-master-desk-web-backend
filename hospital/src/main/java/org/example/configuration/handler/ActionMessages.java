package org.example.configuration.handler;

public enum ActionMessages {

    SAVED("Saved Successfully!"),

    UPDATED("Updated Successfully!"),


    PATIENT_UPDATED_SUCCESSFULLY("Patient Updated Successfully!"),

    DELETED("Deleted Successfully!"),

    REMOVED("Removed Successfully!"),

    ADDED("Added Successfully!"),

    APPROVED("Approved Successfully!"),

    DECLINED("Declined Successfully!"),

    COMPLETED("Declined Successfully!"),

    ACTIVATED("Activated Successfully!"),

    PUBLISHED("Published Successfully!"),

    UN_DECLINED("Un-declined Successfully!"),

    FETCHED("Fetched Successfully!"),

    REJECTED("Rejected Successfully!"),

    UN_REJECTED("Un-rejected Successfully!"),

    REVERSED("Reversed Successfully!"),

    DEACTIVATED("Deactivated Successfully!"),

    CLOSED("Closed Successfully!"),

    RESEND("Re-sent Successfully!"),

    SEND("Sent Successfully!"),

    ISSUED("Issued Successfully");

    public final String label;

    ActionMessages(String label) {

        this.label = label;
    }
}






