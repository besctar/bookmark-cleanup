package org.besctar.bookmark.operation;

import java.util.stream.Stream;

public enum BookmarkOperationType {
    DEAD_URL_REMOVAL("Dead Urls", true),
    DUPLICATE_BY_URL_REMOVAL("Duplicates by Url", true),
    DUPLICATE_BY_NAME_REMOVAL("Duplicates by Name", true),
    GROUP_BY_DOMAIN("Group by Domain", false);

    private String message;
    private boolean cleanup;

    public String getMessage() {
        return message;
    }

    public boolean isCleanup() {
        return cleanup;
    }

    BookmarkOperationType(String message, boolean cleanup) {
        this.message = message;
        this.cleanup = cleanup;
    }

    public static BookmarkOperationType[] cleanupOperations() {
        return Stream.of(values()).filter(BookmarkOperationType::isCleanup).toArray(BookmarkOperationType[]::new);
    }
}
