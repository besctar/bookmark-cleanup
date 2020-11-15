package org.besctar.bookmark.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Data
public class Bookmarks {
    private int version;
    private Map<String, Bookmark> roots;

    @JSONField(serialize = false)
    public Bookmark getBookmarkBar() {
        return getRoots().get("bookmark_bar");
    }

    @JSONField(serialize = false)
    public List<Bookmark> getBBarBookmarkFlat() {
        return getBookmarkBar().flattenChildrenBookmarks();
    }

    @JSONField(serialize = false)
    public List<Bookmark> getBBarDirectoriesFlat() {
        return getBookmarkBar().flattenChildrenDirectories();
    }

    @JSONField(serialize = false)
    public int getBookmarkBarSize() {
        return getBBarBookmarkFlat().size();
    }

    @JSONField(serialize = false)
    public int getMaxId() {
        return getRoots().values()
                .stream()
                .flatMap(it -> it.flattenChildrenStream(bookmark -> true))
                .max(Comparator.comparingInt(Bookmark::getIdInt))
                .map(Bookmark::getIdInt)
                .orElse(0);
    }

    public void applyRemoval(boolean toApply) {
        List<Bookmark> bookmarkList = getBBarBookmarkFlat();
        if (toApply) {
            bookmarkList.forEach(Bookmark::remove);
        } else {
            bookmarkList.forEach(Bookmark::unmarkForRemoval);
        }
    }

    public long forRemovalCount() {
        return getBBarBookmarkFlat()
                .stream()
                .filter(Bookmark::isForRemoval)
                .count();
    }
}
