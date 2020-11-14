package org.besctar.bookmark.handshake;

import java.util.concurrent.CompletableFuture;

public interface BookmarkHandshaker {
    CompletableFuture<UrlHandshake> handshake(String url);
}
