package org.besctar.bookmark.operation;

import org.besctar.bookmark.handshake.BookmarkHandshaker;
import org.besctar.bookmark.model.Bookmark;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CleanupOperationCreator {
    @Inject
    private BookmarkHandshaker bookmarkHandshaker;

    public CleanupOperation create(CleanupType type) {
        switch (type) {
            case REMOVE_DEAD_URL:
                return DeadUrlCleanupOperation.builder()
                        .bkmrkHandshaker(bookmarkHandshaker)
                        .build();
            case REMOVE_DUPLICATE_BY_URL:
                return DuplicateCleanupOperation.builder()
                        .bookmarkAttribute(Bookmark::getUrl)
                        .bookmarkAttributeName("Url")
                        .build();
            case REMOVE_DUPLICATE_BY_NAME:
                return DuplicateCleanupOperation.builder()
                        .bookmarkAttribute(Bookmark::getName)
                        .bookmarkAttributeName("Name")
                        .build();
            default:
                throw new IllegalArgumentException("No such cleanup operation type: " + type);
        }
    }

}
