package org.besctar.bookmark;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import org.besctar.bookmark.cli.CLI;
import org.besctar.bookmark.model.Bookmarks;
import org.besctar.bookmark.operation.BookmarkOperationType;
import org.besctar.bookmark.operation.CleanupOperation;
import org.besctar.bookmark.operation.OperationCreator;
import org.besctar.bookmark.storage.BookmarkStorage;
import org.besctar.bookmark.storage.FileSystemBookmarkStorage;
import org.besctar.bookmark.util.UrlFormat;
import picocli.CommandLine.Command;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Command(name = "bookmark-cleanup",
        description = "Cleanups Chrome bookmarks by several approaches",
        mixinStandardHelpOptions = true)
public class BookmarkCleanupCLI implements Runnable {
    private String bookmarksFile = Path.of(System.getenv("USERPROFILE") + "/AppData/Local/Google/Chrome/User Data/Default/Bookmarks").toString();
    private String workingDir = Path.of(System.getenv("USERPROFILE") + "/Documents/Bookmarks_Output").toString();

    @Inject
    UrlFormat urlFormat;

    @Inject
    CLI cli;

    @Inject
    OperationCreator operationCreator;

    public static void main(String[] args) {
        int exitCode = PicocliRunner.execute(BookmarkCleanupCLI.class, args);
        System.exit(exitCode);
    }

    public void run() {
        cli.println("Started bookmark cleanup process.");

        bookmarksFile = cli.initializePathDialog(bookmarksFile, "Chrome bookmarks file", true);
        workingDir = cli.initializePathDialog(workingDir, "Program working directory root", false);

        BookmarkStorage bkmrkStorage = FileSystemBookmarkStorage
                .builder()
                .bookmarksPath(Path.of(bookmarksFile))
                .workingDirectoryRoot(Path.of(workingDir))
                .urlFormat(urlFormat)
                .build();

        bkmrkStorage.backupBookmarks();
        Bookmarks bookmarks = bkmrkStorage.loadBookmarks();
        int initialBookmarkTotalCount = bookmarks.getBookmarkBarSize();
        cli.println("Bookmarks loaded. Count: {0}", initialBookmarkTotalCount);

        for (BookmarkOperationType type : BookmarkOperationType.cleanupOperations()) {
            if (cli.confirm("Do you want to check up bookmarks for {0}?", type.getMessage())) {
                int bookmarkTotalCount = bookmarks.getBookmarkBarSize();
                CleanupOperation cleanupOperation = operationCreator.create(type);
                long time = System.currentTimeMillis();
                AtomicInteger processedCountAtomic = new AtomicInteger(0);
                cleanupOperation.execute(bookmarks, (bookmark) -> {
                    int processedCount = processedCountAtomic.incrementAndGet();
                    if (bookmark.isForRemoval()) {
                        cli.println("[{0}/{1}], Bookmark is marked to be removed: {2}", processedCount, bookmarkTotalCount, bookmark.getUrl());
                    } else {
                        cli.println("[{0}/{1}] Bookmark is actual status: {2}", processedCount, bookmarkTotalCount, bookmark.getUrl());
                    }
                });
                time = System.currentTimeMillis() - time;
                cli.println("Operation {0} is executed in {1}sec", type.getMessage(), (time / 1000));

                boolean applyRemoval = cli.confirm("Do you want to remove all found trash bookmarks? Count: {0}", bookmarks.forRemovalCount());
                bookmarks.applyRemoval(applyRemoval);
                if (applyRemoval) {
                    int removedCount = bookmarkTotalCount - bookmarks.getBookmarkBarSize();
                    cli.println("Bookmarks cleaned up. Removed count: {0}", removedCount);
                } else {
                    cli.println("Bookmarks removal reverted.");
                }
            }
        }

        if (cli.confirm("Do you want to group/compose bookmarks by domain subfolders?")) {
            int threshold = 3;
            if (cli.confirm("Do you want to change bookmark domain grouping threshold " +
                    "(bookmarks with the same domain will be grouped to subfolder when it's count exceeds or equal the threshold value, " +
                    "default threshold = {0} ):", threshold)) {
                threshold = cli.askForNumberInput("Please specify bookmark grouping threshold (int):");
            }
            CleanupOperation groupByDomainOp = operationCreator.create(BookmarkOperationType.GROUP_BY_DOMAIN, threshold);

            cli.println("Starting bookmarks grouping by domain. Bookmarks count: {0}", bookmarks.getBookmarkBarSize());
            AtomicInteger counter = new AtomicInteger(0);
            groupByDomainOp.execute(bookmarks, bkmk -> {
                int groupedCount = counter.incrementAndGet();
                cli.println("[{0}] Grouped by domain: {1}, bookmark: {2}", groupedCount, bkmk.getParent().getName(), bkmk.getUrl());
            });
            cli.println("Finished bookmarks grouping by domain. Grouped bookmark count: {0}", counter.get());
        }

        cli.println("All cleanup operations done. Saving bookmarks...");
        bkmrkStorage.saveBookmarks(bookmarks);

        cli.askForInput("Resulted bookmarks saved to working directory. Count: {0}\nEnter to finish...", bookmarks.getBookmarkBarSize());
    }
}
