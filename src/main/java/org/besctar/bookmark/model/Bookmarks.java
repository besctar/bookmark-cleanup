package org.besctar.bookmark.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

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
    public List<Bookmark> getBookmarkBarFlat() {
        return getBookmarkBar().flattenChildren();
    }

    @JSONField(serialize = false)
    public int getBookmarkBarSize() {
        return getBookmarkBarFlat().size();
    }

    public void applyRemoval(boolean toApply) {
        List<Bookmark> bookmarkList = getBookmarkBarFlat();
        if (toApply) {
            bookmarkList.forEach(Bookmark::remove);
        } else {
            bookmarkList.forEach(Bookmark::unmarkForRemoval);
        }
    }

    public long forRemovalCount() {
        return getBookmarkBarFlat().stream().filter(Bookmark::isForRemoval).count();
    }
}
