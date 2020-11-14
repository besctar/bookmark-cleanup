package org.besctar.bookmark.storage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.besctar.bookmark.model.Bookmarks;
import org.besctar.bookmark.util.UrlFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Slf4j
public class FileSystemBookmarkStorage implements BookmarkStorage {
    private final Path bookmarksPath;
    private final Path workingDirectoryRoot;
    private final UrlFormat urlFormat;

    private Path workingDirectory;

    @Builder
    public FileSystemBookmarkStorage(Path bookmarksPath, Path workingDirectoryRoot, UrlFormat urlFormat) {
        this.bookmarksPath = bookmarksPath;
        this.workingDirectoryRoot = workingDirectoryRoot;
        this.urlFormat = urlFormat;
    }

    Path workingDirectory() {
        if (workingDirectory != null) return workingDirectory;

        workingDirectory = workingDirectoryRoot
                .resolve(LocalDateTime.now()
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
                        .replace(":", " ")
                );
        workingDirectory.toFile().mkdirs();
        return workingDirectory;
    }

    @Override
    public Bookmarks loadBookmarks() {
        try {
            String bookmarksJson = Files.readString(bookmarksPath);
            Bookmarks bookmarks = JSON.parseObject(bookmarksJson, Bookmarks.class);
            bookmarks.getBookmarkBar().initParentForChildren(urlFormat);
            return bookmarks;
        } catch (Exception ex) {
            log.error("Failed to load bookmarks: " + ex.getLocalizedMessage());
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void saveBookmarks(Bookmarks bookmarks) {
        String json = JSON.toJSONString(bookmarks, SerializerFeature.PrettyFormat);
        try {
            Files.writeString(workingDirectory().resolve("Bookmarks"), json);
            log.info("Updated Bookmarks successfully saved.");
        } catch (IOException ex) {
            log.warn("Cannot save updated bookmarks file", ex);
        }
    }

    @Override
    public void backupBookmarks() {
        try {
            Path backupParent = workingDirectory().resolve("backup");
            backupParent.toFile().mkdirs();
            Files.copy(bookmarksPath, backupParent.resolve("Bookmarks"));
        } catch (IOException ex) {
            log.warn("Error during Bookmarks backup", ex);
        }
    }
}
