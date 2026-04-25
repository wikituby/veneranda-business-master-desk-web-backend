package org.example.statics;

public enum RoleEnums {

    admin("Admin".toUpperCase()),
    customer("Customer".toUpperCase()),
    agent("Agent".toUpperCase()),
    md("Md".toUpperCase());

    public final String label;

    RoleEnums(String label){
        this.label = label;
    }
}






