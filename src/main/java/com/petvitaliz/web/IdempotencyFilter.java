package com.petvitaliz.web;

import com.petvitaliz.config.IdempotencyProperties;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.web.IdempotencyStore.CachedResponse;
import com.petvitaliz.web.IdempotencyStore.Entry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String KEY_HEADER = "Idempotency-Key";
    public static final String REPLAYED_HEADER = "Idempotency-Replayed";

    private static final Set<String> METHODS = Set.of("POST", "PUT", "DELETE");
    private static final int MAX_KEY_LENGTH = 128;
    private static final String NO_FINGERPRINT = "";

    private final IdempotencyStore store;
    private final IdempotencyProperties properties;
    private final ClientIpResolver clientIpResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.enabled()
                || !METHODS.contains(request.getMethod())
                || request.getHeader(KEY_HEADER) == null
                || request.getRequestURI().startsWith("/user/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = request.getHeader(KEY_HEADER).trim();
        if (key.isEmpty() || key.length() > MAX_KEY_LENGTH) {
            JsonResponseWriter.write(response, HttpStatus.BAD_REQUEST,
                    "Idempotency-Key inválida (use de 1 a 128 caracteres)");
            return;
        }

        HttpServletRequest effectiveRequest = request;
        String fingerprint = NO_FINGERPRINT;
        if (hasHashableBody(request)) {
            byte[] body = request.getInputStream().readAllBytes();
            effectiveRequest = new CachedBodyRequest(request, body);
            fingerprint = sha256(body);
        }

        String storeKey = String.join("|", scope(request), request.getMethod(), request.getRequestURI(), key);
        Optional<Entry> existing = store.begin(storeKey, fingerprint);
        if (existing.isPresent()) {
            answerFromExisting(existing.get(), fingerprint, response);
            return;
        }

        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);
        boolean stored = false;
        try {
            chain.doFilter(effectiveRequest, cachingResponse);
            if (HttpStatus.valueOf(cachingResponse.getStatus()).is2xxSuccessful()) {
                store.complete(storeKey, new CachedResponse(
                        cachingResponse.getStatus(),
                        cachingResponse.getContentType(),
                        cachingResponse.getContentAsByteArray()));
                stored = true;
            }
        } finally {
            if (!stored) {
                store.release(storeKey);
            }
            cachingResponse.copyBodyToResponse();
        }
    }

    private void answerFromExisting(Entry entry, String fingerprint, HttpServletResponse response)
            throws IOException {
        if (!entry.getFingerprint().equals(fingerprint)) {
            JsonResponseWriter.write(response, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Esta Idempotency-Key já foi usada com outro conteúdo");
            return;
        }
        if (!entry.isCompleted()) {
            JsonResponseWriter.write(response, HttpStatus.CONFLICT,
                    "Uma requisição com esta Idempotency-Key ainda está sendo processada");
            return;
        }
        CachedResponse cached = entry.getResponse();
        response.setStatus(cached.status());
        response.setHeader(REPLAYED_HEADER, "true");
        if (cached.contentType() != null) {
            response.setContentType(cached.contentType());
        }
        response.setContentLength(cached.body().length);
        response.getOutputStream().write(cached.body());
    }

    private boolean hasHashableBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        long length = request.getContentLengthLong();
        return contentType != null
                && contentType.toLowerCase().startsWith(MediaType.APPLICATION_JSON_VALUE)
                && length >= 0
                && length <= properties.maxBodyBytes();
    }

    private String scope(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.role() + ":" + user.id();
        }
        return "ip:" + clientIpResolver.resolve(request);
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
