package org.besctar.bookmark.handshake;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.besctar.bookmark.model.UrlStatus;
import org.besctar.bookmark.util.UrlFormat;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
@Slf4j
class AsyncHttpClientBookmarkBookmarkHandshaker implements BookmarkHandshaker {
    private static final int MAX_REDIRECT_COUNT = 5;
    private static final int NEW_CONNECTION_TIMEOUT_SEC = 10;
    private static final int REQUEST_TIMEOUT_SEC = 10;

    private AsyncHttpClient httpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder()
            .setConnectTimeout(NEW_CONNECTION_TIMEOUT_SEC * 1000)
            .setRequestTimeout(REQUEST_TIMEOUT_SEC * 1000)
            .setReadTimeout(REQUEST_TIMEOUT_SEC * 1000)
            .build());

    @Override
    public CompletableFuture<UrlHandshake> handshake(String url) {
        return handshakeWithoutRedirect(url)
                .thenCompose(hdsk -> processRedirects(hdsk));
    }

    private CompletableFuture<UrlHandshake> handshakeWithoutRedirect(String url) {
        log.debug("Before handshake " + url);
        CompletableFuture<UrlHandshake> future;
        try {
            future = httpClient.prepareGet(url)
//                    .setFollowRedirect()
                    .execute()
                    .toCompletableFuture()
                    .thenApply(resp -> UrlHandshake.builder()
                            .url(url)
                            .urlStatus(UrlStatus.byStatusCode(resp.getStatusCode()))
                            .resp(resp)
                            .build())
                    .exceptionally(ex -> UrlHandshake.builder()
                            .url(url)
                            .urlStatus(UrlStatus.DEAD)
                            .errorMsg(ex.getLocalizedMessage())
                            .build());
        } catch (RuntimeException ex) {
            future = CompletableFuture.completedFuture(UrlHandshake.builder()
                    .url(UrlFormat.removeTrailingSlash(url))
                    .urlStatus(UrlStatus.INVALID_URL_FORMAT)
                    .errorMsg(ex.getLocalizedMessage())
                    .build());
        }
        return future.thenApply(hdsk -> {
            if (hdsk.isError()) {
                log.debug("Handshake error: statusCode = {}, url = {}, msg={}", hdsk.getStatusCode(), hdsk.getUrl(), hdsk.getErrorMsg());
            } else {
                log.debug("Handshake ok: statusCode = {}, url = {}", hdsk.getStatusCode(), hdsk.getUrl());
            }
            return hdsk;
        });
    }

    private CompletableFuture<UrlHandshake> processRedirects(UrlHandshake urlHandshake) {
        return processRedirects(urlHandshake, 0);
    }

    private CompletableFuture<UrlHandshake> processRedirects(UrlHandshake urlHandshake, int redirectNumber) {
        if (urlHandshake.getUrlStatus() != UrlStatus.REDIRECTED || redirectNumber >= MAX_REDIRECT_COUNT) {
            log.info("Redirect resolved: {}", urlHandshake.getUrl());
            return CompletableFuture.completedFuture(urlHandshake);
        }
        log.info("Redirect accepted: {}", urlHandshake.getUrl());
        String redirectUrl = redirectUrl(urlHandshake);

        if (urlHandshake.getUrl().equalsIgnoreCase(redirectUrl)) {
            return CompletableFuture.completedFuture(urlHandshake);
        }

        return handshake(redirectUrl)
                .thenCompose(urlHandshake1 -> processRedirects(urlHandshake1, redirectNumber + 1));
    }

    private String redirectUrl(UrlHandshake urlHandshake) {
        List<String> locations = urlHandshake.getResp().getHeaders("Location");
        if (locations.size() > 0)
            return locations.get(locations.size() - 1);
        else
            return urlHandshake.getUrl();
    }
}
