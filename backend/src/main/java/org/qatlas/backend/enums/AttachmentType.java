package org.qatlas.backend.enums;

public enum AttachmentType {
    SNAPSHOT("Snapshot"),
    OTHER( "Other");

    private final String name;

    AttachmentType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
