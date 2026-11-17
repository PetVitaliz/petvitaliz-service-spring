package com.petvitaliz.web;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong nextCleanupAt = new AtomicLong();
    private final Clock clock;

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    public Decision consume(String key, int limit, Duration windowSize) {
        long now = clock.millis();
        long sizeMillis = windowSize.toMillis();

        Window window = windows.compute(key, (ignored, current) ->
                current == null || now >= current.resetAt()
                        ? new Window(now + sizeMillis, 1)
                        : new Window(current.resetAt(), current.count() + 1));

        removeExpired(now, sizeMillis);

        long retryAfterSeconds = Math.max(1, (window.resetAt() - now + 999) / 1000);
        int remaining = Math.max(0, limit - window.count());
        return new Decision(window.count() <= limit, limit, remaining, retryAfterSeconds);
    }

    private void removeExpired(long now, long sizeMillis) {
        long scheduled = nextCleanupAt.get();
        if (now >= scheduled && nextCleanupAt.compareAndSet(scheduled, now + sizeMillis)) {
            windows.values().removeIf(window -> now >= window.resetAt());
        }
    }

    private record Window(long resetAt, int count) {
    }

    public record Decision(boolean allowed, int limit, int remaining, long retryAfterSeconds) {
    }
}
