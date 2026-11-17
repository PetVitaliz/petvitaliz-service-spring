package com.petvitaliz.web;

import com.petvitaliz.config.IdempotencyProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
    private final Clock clock;
    private final long ttlMillis;
    private final int maxEntries;

    public IdempotencyStore(Clock clock, IdempotencyProperties properties) {
        this.clock = clock;
        this.ttlMillis = properties.ttl().toMillis();
        this.maxEntries = properties.maxEntries();
    }

    public Optional<Entry> begin(String key, String fingerprint) {
        long now = clock.millis();
        makeRoom(now);

        Entry created = new Entry(fingerprint, now + ttlMillis);
        Entry stored = entries.compute(key, (ignored, current) ->
                current == null || now >= current.getExpiresAt() ? created : current);
        return stored == created ? Optional.empty() : Optional.of(stored);
    }

    public void complete(String key, CachedResponse response) {
        Entry entry = entries.get(key);
        if (entry != null) {
            entry.response = response;
        }
    }

    public void release(String key) {
        entries.remove(key);
    }

    private void makeRoom(long now) {
        if (entries.size() < maxEntries) {
            return;
        }
        entries.values().removeIf(entry -> now >= entry.getExpiresAt());
        if (entries.size() >= maxEntries) {
            entries.entrySet().stream()
                    .min(Comparator.comparingLong(entry -> entry.getValue().getExpiresAt()))
                    .map(Map.Entry::getKey)
                    .ifPresent(entries::remove);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static final class Entry {

        private final String fingerprint;
        private final long expiresAt;
        private volatile CachedResponse response;

        public boolean isCompleted() {
            return response != null;
        }
    }

    public record CachedResponse(int status, String contentType, byte[] body) {
    }
}
