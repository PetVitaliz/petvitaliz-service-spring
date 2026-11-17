package com.petvitaliz.web;

import com.petvitaliz.config.IdempotencyProperties;
import com.petvitaliz.config.RateLimitProperties;
import com.petvitaliz.support.MutableClock;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyFilterTest {

    private static final Duration TTL = Duration.ofHours(1);

    private final MutableClock clock = new MutableClock(LocalDateTime.of(2026, 6, 15, 14, 0), ZoneId.of("UTC"));
    private final IdempotencyProperties properties = new IdempotencyProperties(true, TTL, 100, 1024);
    private final IdempotencyFilter filter = new IdempotencyFilter(
            new IdempotencyStore(clock, properties),
            properties,
            new ClientIpResolver(new RateLimitProperties(true, 1, 1, Duration.ofMinutes(1), 0)));
    private final AtomicInteger handled = new AtomicInteger();

    @Test
    void replaysTheFirstResponseForTheSameKey() throws Exception {
        MockHttpServletResponse first = call("POST", "/agendamento", "key-1", "{\"a\":1}", 201);
        MockHttpServletResponse second = call("POST", "/agendamento", "key-1", "{\"a\":1}", 201);

        assertThat(handled).hasValue(1);
        assertThat(first.getHeader(IdempotencyFilter.REPLAYED_HEADER)).isNull();
        assertThat(second.getHeader(IdempotencyFilter.REPLAYED_HEADER)).isEqualTo("true");
        assertThat(second.getStatus()).isEqualTo(201);
        assertThat(second.getContentAsString()).isEqualTo(first.getContentAsString()).isEqualTo("{\"call\":1}");
    }

    @Test
    void rejectsTheSameKeyWithAnotherBody() throws Exception {
        call("POST", "/agendamento", "key-2", "{\"a\":1}", 201);

        assertThat(call("POST", "/agendamento", "key-2", "{\"a\":2}", 201).getStatus()).isEqualTo(422);
        assertThat(handled).hasValue(1);
    }

    @Test
    void doesNotStoreFailedResponses() throws Exception {
        assertThat(call("POST", "/agendamento", "key-3", "{}", 409).getStatus()).isEqualTo(409);
        assertThat(call("POST", "/agendamento", "key-3", "{}", 201).getStatus()).isEqualTo(201);
        assertThat(handled).hasValue(2);
    }

    @Test
    void keysAreScopedByPathAndExpireAfterTheTtl() throws Exception {
        call("POST", "/agendamento", "key-4", "{}", 201);
        call("POST", "/user/planos/pagamento", "key-4", "{}", 200);
        assertThat(handled).hasValue(2);

        clock.advance(TTL);
        call("POST", "/agendamento", "key-4", "{}", 201);
        assertThat(handled).hasValue(3);
    }

    @Test
    void ignoresRequestsWithoutKeyReadsAndLoginRoutes() throws Exception {
        call("POST", "/agendamento", null, "{}", 201);
        call("POST", "/agendamento", null, "{}", 201);
        call("GET", "/consultas", "key-5", "", 200);
        call("GET", "/consultas", "key-5", "", 200);
        call("POST", "/user/login", "key-5", "{}", 200);
        call("POST", "/user/login", "key-5", "{}", 200);

        assertThat(handled).hasValue(6);
    }

    @Test
    void rejectsOversizedKeys() throws Exception {
        assertThat(call("POST", "/agendamento", "k".repeat(129), "{}", 201).getStatus()).isEqualTo(400);
        assertThat(handled).hasValue(0);
    }

    @Test
    void handlerStillReadsTheRequestBody() throws Exception {
        MockHttpServletRequest request = request("POST", "/agendamento", "key-6", "{\"nome\":\"Rex\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        StringBuilder seen = new StringBuilder();

        filter.doFilter(request, response, (req, res) ->
                seen.append(new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8)));

        assertThat(seen).hasToString("{\"nome\":\"Rex\"}");
    }

    private MockHttpServletResponse call(String method, String path, String key, String body, int status)
            throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) res;
            httpResponse.setStatus(status);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().write("{\"call\":" + handled.incrementAndGet() + "}");
        };
        filter.doFilter(request(method, path, key, body), response, chain);
        return response;
    }

    private static MockHttpServletRequest request(String method, String path, String key, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr("10.0.0.1");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        if (key != null) {
            request.addHeader(IdempotencyFilter.KEY_HEADER, key);
        }
        return request;
    }
}
