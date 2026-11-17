package com.petvitaliz.web;

import com.petvitaliz.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ClientIpResolver {

    private static final String FORWARDED_FOR = "X-Forwarded-For";

    private final int trustedProxies;

    public ClientIpResolver(RateLimitProperties properties) {
        this.trustedProxies = properties.trustedProxies();
    }

    public String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR);
        if (trustedProxies <= 0 || forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        List<String> hops = Arrays.stream(forwardedFor.split(","))
                .map(String::trim)
                .filter(hop -> !hop.isEmpty())
                .toList();
        int clientIndex = hops.size() - 1 - trustedProxies;
        if (hops.isEmpty()) {
            return request.getRemoteAddr();
        }
        return hops.get(Math.max(0, clientIndex));
    }
}
