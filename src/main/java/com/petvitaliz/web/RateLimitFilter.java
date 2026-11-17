package com.petvitaliz.web;

import com.petvitaliz.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    public static final String LIMIT_HEADER = "X-RateLimit-Limit";
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";

    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;
    private final ClientIpResolver clientIpResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.enabled()
                || HttpMethod.OPTIONS.matches(request.getMethod())
                || "/health".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        boolean authRoute = isAuthRoute(request);
        String key = (authRoute ? "auth:" : "general:") + clientIpResolver.resolve(request);
        int limit = authRoute ? properties.authLimit() : properties.generalLimit();

        RateLimiter.Decision decision = rateLimiter.consume(key, limit, properties.window());
        response.setHeader(LIMIT_HEADER, String.valueOf(decision.limit()));
        response.setHeader(REMAINING_HEADER, String.valueOf(decision.remaining()));

        if (!decision.allowed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(decision.retryAfterSeconds()));
            JsonResponseWriter.write(response, HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas requisições. Tente novamente em alguns instantes");
            return;
        }

        chain.doFilter(request, response);
    }

    private static boolean isAuthRoute(HttpServletRequest request) {
        String path = request.getRequestURI();
        return HttpMethod.POST.matches(request.getMethod())
                && (path.startsWith("/user/login") || path.equals("/user/cadastro"));
    }
}
