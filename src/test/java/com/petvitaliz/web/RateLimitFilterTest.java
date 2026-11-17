package com.petvitaliz.web;

import com.petvitaliz.config.RateLimitProperties;
import com.petvitaliz.support.MutableClock;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final MutableClock clock = new MutableClock(LocalDateTime.of(2026, 6, 15, 14, 0), ZoneId.of("UTC"));

    @Test
    void blocksRequestsOverTheLimitAndRecoversAfterTheWindow() throws Exception {
        RateLimitFilter filter = filter(new RateLimitProperties(true, 2, 2, WINDOW, 0));

        assertThat(call(filter, "GET", "/user/home", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call(filter, "GET", "/user/home", "10.0.0.1").getHeader(RateLimitFilter.REMAINING_HEADER))
                .isEqualTo("0");

        MockHttpServletResponse blocked = call(filter, "GET", "/user/home", "10.0.0.1");
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("60");
        assertThat(blocked.getContentAsString()).contains("mensagem");

        clock.advance(WINDOW);
        assertThat(call(filter, "GET", "/user/home", "10.0.0.1").getStatus()).isEqualTo(200);
    }

    @Test
    void countsEachClientSeparately() throws Exception {
        RateLimitFilter filter = filter(new RateLimitProperties(true, 1, 1, WINDOW, 0));

        assertThat(call(filter, "GET", "/", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call(filter, "GET", "/", "10.0.0.1").getStatus()).isEqualTo(429);
        assertThat(call(filter, "GET", "/", "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    void loginRoutesUseTheStricterLimit() throws Exception {
        RateLimitFilter filter = filter(new RateLimitProperties(true, 100, 1, WINDOW, 0));

        assertThat(call(filter, "POST", "/user/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call(filter, "POST", "/user/login/adm", "10.0.0.1").getStatus()).isEqualTo(429);
        assertThat(call(filter, "GET", "/user/home", "10.0.0.1").getStatus()).isEqualTo(200);
    }

    @Test
    void skipsPreflightHealthCheckAndDisabledConfiguration() throws Exception {
        RateLimitFilter strict = filter(new RateLimitProperties(true, 0, 0, WINDOW, 0));
        RateLimitFilter disabled = filter(new RateLimitProperties(false, 0, 0, WINDOW, 0));

        assertThat(call(strict, "OPTIONS", "/agendamento", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call(strict, "GET", "/health", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(call(strict, "GET", "/", "10.0.0.1").getStatus()).isEqualTo(429);
        assertThat(call(disabled, "GET", "/", "10.0.0.1").getStatus()).isEqualTo(200);
    }

    @Test
    void resolvesTheClientBehindTrustedProxies() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setRemoteAddr("10.0.0.9");
        request.addHeader("X-Forwarded-For", "1.1.1.1, 203.0.113.7, 172.16.0.1");

        assertThat(resolver(0).resolve(request)).isEqualTo("10.0.0.9");
        assertThat(resolver(1).resolve(request)).isEqualTo("203.0.113.7");
        assertThat(resolver(2).resolve(request)).isEqualTo("1.1.1.1");
        assertThat(resolver(5).resolve(request)).isEqualTo("1.1.1.1");
    }

    private RateLimitFilter filter(RateLimitProperties properties) {
        return new RateLimitFilter(new RateLimiter(clock), properties, new ClientIpResolver(properties));
    }

    private static ClientIpResolver resolver(int trustedProxies) {
        return new ClientIpResolver(new RateLimitProperties(true, 1, 1, WINDOW, trustedProxies));
    }

    private static MockHttpServletResponse call(RateLimitFilter filter, String method, String path, String ip)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
