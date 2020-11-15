package org.besctar.bookmark.operation;

import org.besctar.bookmark.handshake.BookmarkHandshaker;
import org.besctar.bookmark.model.Bookmark;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OperationCreator {
    @Inject
    private BookmarkHandshaker bookmarkHandshaker;

    public CleanupOperation create(BookmarkOperationType type, Object... params) {
        switch (type) {
            case DEAD_URL_REMOVAL:
                return DeadUrlCleanupOperation.builder()
                        .bkmrkHandshaker(bookmarkHandshaker)
                        .build();
            case DUPLICATE_BY_URL_REMOVAL:
                return DuplicateCleanupOperation.builder()
                        .bookmarkAttribute(Bookmark::getUrl)
                        .bookmarkAttributeName("Url")
                        .build();
            case DUPLICATE_BY_NAME_REMOVAL:
                return DuplicateCleanupOperation.builder()
                        .bookmarkAttribute(Bookmark::getName)
                        .bookmarkAttributeName("Name")
                        .build();
            case GROUP_BY_DOMAIN:
                GroupByDomainOperation.GroupByDomainOperationBuilder builder = GroupByDomainOperation.builder();
                if (params.length > 0) {
                    builder.domainGroupingThreshold((Integer) params[0]);
                }
                return builder.build();
            default:
                throw new IllegalArgumentException("No such cleanup operation type: " + type);
        }
    }

}
