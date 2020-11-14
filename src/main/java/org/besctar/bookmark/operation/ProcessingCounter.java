package org.besctar.bookmark.operation;

import org.besctar.bookmark.model.UrlStatus;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ProcessingCounter {
    private volatile EnumMap<UrlStatus, AtomicInteger> processedByStatus = new EnumMap<>(UrlStatus.class);
    private volatile AtomicInteger processedCount = new AtomicInteger(0);
    private final int total;

    public ProcessingCounter(int total) {
        this.total = total;
        Stream.of(UrlStatus.values()).forEach(it -> processedByStatus.put(it, new AtomicInteger(0)));
    }

    public int incrementAndGet(UrlStatus urlStatus) {
        processedByStatus.get(urlStatus).incrementAndGet();
        return processedCount.incrementAndGet();
    }

    public int getCountBy(UrlStatus status) {
        return processedByStatus.get(status).get();
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public int getTotal() {
        return total;
    }
}
