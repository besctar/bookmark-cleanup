package org.besctar.bookmark.handshake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.asynchttpclient.Response;
import org.besctar.bookmark.model.UrlStatus;

@Data
@Builder
@AllArgsConstructor
public class UrlHandshake {
    private String url;
    private UrlStatus urlStatus;
    private Response resp;
    private String errorMsg;

    public boolean isError() {
        return errorMsg != null;
    }

    public int getStatusCode() {
        return resp == null ? 500 : resp.getStatusCode();
    }
}
