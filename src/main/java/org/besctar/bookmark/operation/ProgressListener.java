package org.besctar.bookmark.operation;

import org.besctar.bookmark.model.Bookmark;

public interface ProgressListener {
    void processed(Bookmark bookmark);
}
