package org.besctar.bookmark.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.besctar.bookmark.util.UrlFormat;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Bookmark {
    private String id;
    private String name;
    private String type;
    private String guid;
    private String url;
    @JSONField(name = "date_added")
    private String dateAdded;
    @JSONField(name = "date_modified")
    private String dateModified;
    private List<Bookmark> children;

    private transient Bookmark parent;
    private transient volatile boolean forRemoval;
    private transient URI uri;

    public void init(UrlFormat urlFormat) {
        this.uri = urlFormat.sanitizeUrlAndParams(this.url);
        this.url = UrlFormat.removeTrailingSlash(uri.toString());
    }

    public List<Bookmark> getChildren() {
        if (children == null) {
            if (isDirectory()) {
                children = new ArrayList<>();
            }
        }
        return children;
    }

    @JSONField(serialize = false)
    public int getIdInt() {
        return Integer.parseInt(getId());
    }

    @JSONField(serialize = false)
    public String getDomain() {
        return uri.getHost();
    }

    @JSONField(serialize = false)
    public boolean isDirectory() {
        return BookmarkType.FOLDER.getName().equalsIgnoreCase(type);
    }

    @JSONField(serialize = false)
    public boolean isBookmark() {
        return BookmarkType.URL.getName().equalsIgnoreCase(type);
    }

    @JSONField(serialize = false)
    public String getScheme() {
        return uri.getScheme();
    }

    public void markForRemoval() {
        this.forRemoval = true;
    }

    public void unmarkForRemoval() {
        this.forRemoval = false;
    }

    public void remove() {
        if (!forRemoval) return;
        Optional.ofNullable(parent)
                .ifPresent(it -> {
                    it.getChildren().remove(this);
                    parent = null;
                    forRemoval = false;
                    log.info("Bookmark removed: " + getUrl());
                });
    }

    public void moveToParent(Bookmark otherParent) {
        if (!otherParent.isDirectory()) return;
        parent.getChildren().remove(this);

        parent = otherParent;
        parent.getChildren().add(this);
    }

    public void initParentForChildren(UrlFormat urlFormat) {
        Optional.ofNullable(children)
                .stream()
                .flatMap(Collection::stream)
                .forEach(ch -> {
                    if (!ch.isDirectory()) {
                        ch.init(urlFormat);
                    }
                    ch.setParent(this);
                    ch.initParentForChildren(urlFormat);
                });
    }

    @JSONField(serialize = false)
    public List<Bookmark> flattenChildrenBookmarks() {
        return flattenChildrenStream(Bookmark::isBookmark).collect(Collectors.toList());
    }

    @JSONField(serialize = false)
    public List<Bookmark> flattenChildrenDirectories() {
        return flattenChildrenStream(Bookmark::isDirectory).collect(Collectors.toList());
    }

    public List<Bookmark> childrenByType(BookmarkType type) {
        return Optional.ofNullable(children)
                .orElse(Collections.emptyList())
                .stream()
                .filter(it -> Objects.equals(it.type, type.getName()))
                .collect(Collectors.toList());
    }

    Stream<Bookmark> flattenChildrenStream(Function<Bookmark, Boolean> predicate) {
        Stream<Bookmark> selfStream = Stream.of(this)
                .filter(it -> predicate.apply(it));
        Stream<Bookmark> childrenStream = Optional.ofNullable(this.children)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(it -> it.flattenChildrenStream(predicate));
        return Stream.concat(
                selfStream,
                childrenStream);
    }
}
