package com.petvitaliz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("300") int generalLimit,
        @DefaultValue("20") int authLimit,
        @DefaultValue("1m") Duration window,
        @DefaultValue("0") int trustedProxies
) {
}
