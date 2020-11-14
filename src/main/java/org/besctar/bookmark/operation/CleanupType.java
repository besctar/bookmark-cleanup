package org.besctar.bookmark.operation;

public enum CleanupType {
    REMOVE_DEAD_URL("Dead Urls"),
    REMOVE_DUPLICATE_BY_URL("Duplicates by Url"),
    REMOVE_DUPLICATE_BY_NAME("Duplicates by Name");

    private String message;

    public String getMessage() {
        return message;
    }

    private CleanupType(String message) {
        this.message = message;
    }
}
