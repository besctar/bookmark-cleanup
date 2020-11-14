package org.besctar.bookmark.model;

import com.alibaba.fastjson.annotation.JSONField;
import io.micronaut.core.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.besctar.bookmark.util.UrlFormat;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
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

    @JSONField(serialize = false)
    public String getDomain() {
        return uri.getHost();
    }

    @JSONField(serialize = false)
    public boolean isDirectory() {
        return "folder".equalsIgnoreCase(type);
    }

    @JSONField(serialize = false)
    public boolean isBookmark() {
        return "url".equalsIgnoreCase(type);
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
                    it.children.remove(this);
                    parent = null;
                    log.info("Bookmark removed: " + getUrl());
                });
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
    public List<Bookmark> flattenChildren() {
        return flattenChildrenStream().collect(Collectors.toList());
    }

    private Stream<Bookmark> flattenChildrenStream() {
        if (StringUtils.hasText(url)) {
            return Stream.of(this);
        }
        return Optional.ofNullable(children)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(it -> it.flattenChildrenStream());
    }
}
