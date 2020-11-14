package org.besctar.bookmark;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import org.besctar.bookmark.cli.CLI;
import org.besctar.bookmark.util.UrlFormat;

import javax.inject.Singleton;
import java.util.Optional;

@Factory
public class BookmarkBeanFactory {
    @Value("${escapeParams}")
    String escapeParams;

    public String[] getEscapeParams() {
        return Optional.ofNullable(escapeParams)
                .map(it -> it.split(","))
                .orElse(new String[]{});
    }

    @Singleton
    public CLI cli() {
        return new CLI(System.in, System.out);
    }

    @Singleton
    public UrlFormat urlFormat() {
        return new UrlFormat(getEscapeParams());
    }
}
