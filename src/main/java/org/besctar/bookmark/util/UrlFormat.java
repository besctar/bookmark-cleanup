package org.besctar.bookmark.util;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UrlFormat {
    String[] escapeParams;

    public UrlFormat(String[] escapeParams) {
        this.escapeParams = escapeParams;
    }

    public static String removeTrailingSlash(String s) {
        s = removeTrailing(s, "/");
        return s;
    }

    private static String removeTrailing(String s, String trailingChar) {
        return s.endsWith(trailingChar) ? s.substring(0, s.length() - 1) : s;
    }

    public URI sanitizeUrlAndParams(String s) {
        return sanitizeUrlAndParams(s, escapeParams);
    }

    private static URI sanitizeUrlAndParams(String s, String... paramsToExcape) {
        URI uri = sanitize(s);
        URI uriCleaned = escapeQueryParams(uri, paramsToExcape);

        return uriCleaned;
    }

    public static URI sanitize(String urlString) {
        urlString = urlString.startsWith("http") ? urlString : "http://" + urlString;
        urlString = urlString.replaceAll("[^\\x00-\\x7F]", "");
        urlString = removeTrailingSlash(urlString);
        try {
            String decodedURL = URLDecoder.decode(urlString, "UTF-8");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri;
        } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static URI escapeQueryParams(URI uri, String... paramsToEscape) {
        Set<String> paramsToEscapeSet = Set.of(paramsToEscape);

        String query = uri.getQuery();
        if (query == null || query.isEmpty())
            return uri;
        String queryCleaned = Stream.of(query.split("&"))
                .map(part -> part.split("="))
                .filter(parts -> !paramsToEscapeSet.contains(parts[0]))
                .map(parts -> parts.length > 1 ? parts[0] + "=" + parts[1] : parts[0])
                .collect(Collectors.joining("&"));
        queryCleaned = queryCleaned.isEmpty() ? null : queryCleaned;
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), queryCleaned, uri.getFragment());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
