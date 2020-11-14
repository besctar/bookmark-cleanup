package org.besctar.bookmark.storage;

import org.besctar.bookmark.model.Bookmarks;

public interface BookmarkStorage {
    Bookmarks loadBookmarks();

    void saveBookmarks(Bookmarks bookmarks);

    void backupBookmarks();
}
