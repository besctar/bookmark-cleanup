package org.besctar.bookmark.operation;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.besctar.bookmark.model.Bookmark;
import org.besctar.bookmark.model.Bookmarks;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class DuplicateCleanupOperation implements CleanupOperation {
    static Comparator<Bookmark> BY_URL_LENGTH = Comparator.comparing(a -> a.getUrl().length());

    private final Function<Bookmark, String> bookmarkAttribute;
    private final String bookmarkAttributeName;

    public DuplicateCleanupOperation(Function<Bookmark, String> bookmarkAttribute, String bookmarkAttributeName) {
        this.bookmarkAttribute = bookmarkAttribute;
        this.bookmarkAttributeName = bookmarkAttributeName;
    }

    @Override
    public void execute(Bookmarks bookmarks, ProgressListener progressListener) {
        List<Bookmark> bookmarkList = bookmarks.getBookmarkBarFlat();
        long urlsCount = bookmarkList.size();
        Map<String, List<Bookmark>> groupingByAttrMap = bookmarkList.stream().collect(Collectors.groupingBy(bookmarkAttribute));

        log.info("Duplicates check by {}. Duplicates count: {}", bookmarkAttributeName, (urlsCount - groupingByAttrMap.size()));

        groupingByAttrMap.entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 1) // duplicates
                .forEach(e -> e.getValue()
                        .stream()
                        .sorted(BY_URL_LENGTH)
                        .skip(1) // remain the only unique url
                        .forEach(Bookmark::markForRemoval));
        for (Bookmark bookmark : bookmarkList) {
            progressListener.processed(bookmark);
            if (bookmark.isForRemoval()) {
                log.info("Bookmark marked as duplicate for removal: {}", bookmark.getUrl());
            }
        }
    }
}
