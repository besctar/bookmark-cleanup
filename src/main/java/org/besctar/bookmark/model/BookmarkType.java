package org.besctar.bookmark.model;

public enum BookmarkType {
    URL("url"), FOLDER("folder");

    BookmarkType(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }
}
