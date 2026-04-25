package org.example.statics;

public enum UserTypes {

    AGENT("Agent".toUpperCase()),

    ADMIN("Admin".toUpperCase()),

    CUSTOMER("Client".toUpperCase());

    public final String label;

    UserTypes(String label) {

        this.label = label;
    }
}






