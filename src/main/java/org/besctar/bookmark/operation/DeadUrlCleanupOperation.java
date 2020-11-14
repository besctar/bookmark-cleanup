package org.besctar.bookmark.operation;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.besctar.bookmark.handshake.BookmarkHandshaker;
import org.besctar.bookmark.model.Bookmark;
import org.besctar.bookmark.model.Bookmarks;
import org.besctar.bookmark.model.UrlStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class DeadUrlCleanupOperation implements CleanupOperation {
    private BookmarkHandshaker bkmrkHandshaker;

    @Override
    public void execute(Bookmarks bookmarks, ProgressListener progressListener) {
        log.info("Starting lookup for DEAD URLs...");
        log.info("Also urls will be process REDIRECTs and update itself...");
        log.info("Trash query params (specified in escapeQueryParams) will be cleaned up...");
        List<Bookmark> bookmarkList = bookmarks.getBookmarkBarFlat();
        ProcessingCounter counter = new ProcessingCounter(bookmarkList.size());

        List<CompletableFuture<UrlStatus>> urlCheckFutureList = bookmarkList.stream()
                .map(b -> processBookmarkHandshake(b, counter, progressListener))
                .collect(Collectors.toList());

        CompletableFuture.allOf(urlCheckFutureList.toArray(CompletableFuture[]::new))
                .join();

        log.info("Finally ALIVE URLs count: " + counter.getCountBy(UrlStatus.OK));
        log.info("Deleted Infinite REDIRECT URLs count: " + counter.getCountBy(UrlStatus.REDIRECTED));
        log.info("Deleted UNCLEAR URLs count (codes 300-403): " + counter.getCountBy(UrlStatus.UNCLEAR));
        log.info("Deleted URLs count because dead: " + counter.getCountBy(UrlStatus.DEAD));
        log.info("Deleted URLs count because invalid url format: " + counter.getCountBy(UrlStatus.INVALID_URL_FORMAT));
        log.info("Total deleted Urls: " + (counter.getTotal() - counter.getCountBy(UrlStatus.OK)));
    }

    private CompletableFuture<UrlStatus> processBookmarkHandshake(Bookmark bookmark, ProcessingCounter counter, ProgressListener progressListener) {
        String url = bookmark.getUrl();

        return bkmrkHandshaker.handshake(url)
                .thenApply(urlHandshake -> {
                    UrlStatus urlStatus = urlHandshake.getUrlStatus();
                    if (urlStatus.isToDelete()) {
                        bookmark.markForRemoval();
                    }
                    bookmark.setUrl(urlHandshake.getUrl());
                    progressListener.processed(bookmark);
                    log.info("Bookmark status {}: {}", urlHandshake.getUrlStatus(), urlHandshake.getUrl());
                    return urlStatus;
                });
    }
}
