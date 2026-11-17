package com.petvitaliz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("app.idempotency")
public record IdempotencyProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("24h") Duration ttl,
        @DefaultValue("10000") int maxEntries,
        @DefaultValue("1048576") int maxBodyBytes
) {
}
