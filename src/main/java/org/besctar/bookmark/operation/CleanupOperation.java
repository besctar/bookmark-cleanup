package org.besctar.bookmark.operation;

import org.besctar.bookmark.model.Bookmarks;

public interface CleanupOperation {
    void execute(Bookmarks bookmarks, ProgressListener progressListener);
}
