package com.petvitaliz.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MutableClock extends Clock {

    private final ZoneId zone;
    private final Instant start;
    private volatile Instant current;

    public MutableClock(LocalDateTime start, ZoneId zone) {
        this.zone = zone;
        this.start = start.atZone(zone).toInstant();
        this.current = this.start;
    }

    public void set(LocalDateTime dateTime) {
        current = dateTime.atZone(zone).toInstant();
    }

    public void advance(Duration duration) {
        current = current.plus(duration);
    }

    public void reset() {
        current = start;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return Clock.fixed(current, newZone);
    }

    @Override
    public Instant instant() {
        return current;
    }
}
