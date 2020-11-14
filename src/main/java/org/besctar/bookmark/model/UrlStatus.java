package org.besctar.bookmark.model;

import lombok.Getter;

@Getter
public enum UrlStatus {
    OK(200, 201, false, "ALIVE, statusCode={1}, url={0}"),
    REDIRECTED(300, 304, true, "REDIRECT, statusCode={1}, url={0}"),
    UNCLEAR(201, 404, true, "UNCLEAR, statusCode={1}, url={0}"),
    DEAD(404, 600, true, "DEAD, statusCode={1}, url={0}"),
    INVALID_URL_FORMAT(600, 601, true, "INVALID FORMAT, statusCode={1}, url={0}");

    UrlStatus(int min, int max, boolean toDelete, String msg) {
        this.min = min;
        this.max = max;
        this.toDelete = toDelete;
        this.messageTemplate = msg;
    }

    private final int min; // inclusive
    private final int max; // exclusive
    private final boolean toDelete;
    private final String messageTemplate;

    public boolean checkStatusCode(int code) {
        return code >= min && code < max;
    }

    public static UrlStatus byStatusCode(int code) {
        for (UrlStatus status : values()) {
            if (status.checkStatusCode(code)) return status;
        }
        throw new IllegalArgumentException("No such UrlStatus exist for the code = " + code);
    }
}
