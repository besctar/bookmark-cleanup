package org.besctar.bookmark.operation;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.besctar.bookmark.model.Bookmark;
import org.besctar.bookmark.model.BookmarkType;
import org.besctar.bookmark.model.Bookmarks;

import java.util.*;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class GroupByDomainOperation implements CleanupOperation {
    static Comparator<Bookmark> BY_URL_LENGTH = Comparator.comparing(a -> a.getUrl().length());

    private int domainGroupingThreshold;

    @Override
    public void execute(Bookmarks bookmarks, ProgressListener progressListener) {
        List<Bookmark> directories = bookmarks.getBBarDirectoriesFlat();

        for (Bookmark dir : directories) {
            List<Bookmark> dirChildren = dir.childrenByType(BookmarkType.URL);
            Map<String, List<Bookmark>> groupingByAttrMap = dirChildren.stream().collect(Collectors.groupingBy(Bookmark::getDomain));

            for (Map.Entry<String, List<Bookmark>> entry : groupingByAttrMap.entrySet()) {
                if (entry.getValue().size() >= domainGroupingThreshold) {
                    String domain = entry.getKey();
                    String currentTime = String.valueOf(System.currentTimeMillis());
                    Bookmark domainSubfolder = Bookmark.builder()
                            .type(BookmarkType.FOLDER.getName())
                            .children(new ArrayList<>())
                            .name(domain)
                            .parent(dir)
                            .dateAdded(currentTime)
                            .dateModified(currentTime)
                            .guid(UUID.randomUUID().toString())
                            .build();

                    dir.getChildren().add(0, domainSubfolder);
                    entry.getValue().forEach(it -> it.moveToParent(domainSubfolder));

                    entry.getValue().forEach(progressListener::processed);
                }
            }
            // + propagate threshold from main CLI
            // + implement flat bookmark method - without recursion in depth
            // + group by domain only this dir
            // + filter domains with count > threshold
            // + getMaxId in current bookmarks
            // + implement method bookmark.move(otherParent)
            // + create folder with such domain - first position in this dir
            // + move all bookmarks filtered to domain dir
        }
    }
}
