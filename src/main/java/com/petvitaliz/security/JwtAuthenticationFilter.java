package com.petvitaliz.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String INVALID_TOKEN_ATTRIBUTE = "petvitaliz.invalidToken";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        TokenType type = TokenType.forPath(request.getRequestURI());

        List<String> tokens = Stream.of(readCookie(request, type.getCookieName()), readBearer(request))
                .flatMap(Optional::stream)
                .toList();

        boolean authenticated = tokens.stream().anyMatch(token -> tryAuthenticate(type, token));
        if (!authenticated && !tokens.isEmpty()) {
            request.setAttribute(INVALID_TOKEN_ATTRIBUTE, true);
        }

        chain.doFilter(request, response);
    }

    private boolean tryAuthenticate(TokenType type, String token) {
        try {
            authenticate(type, jwtService.parse(token));
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private void authenticate(TokenType type, Claims claims) {
        if (!type.getRole().equals(claims.get(JwtClaims.ROLE, String.class))) {
            throw new JwtException("Wrong token type");
        }
        if (type == TokenType.PASSWORD_RESET
                && !Boolean.TRUE.equals(claims.get(JwtClaims.RESET_AUTHORIZED, Boolean.class))) {
            throw new JwtException("Password reset not authorized");
        }

        AuthenticatedUser principal = new AuthenticatedUser(
                claims.get(JwtClaims.ID, Integer.class),
                claims.get(JwtClaims.EMAIL, String.class),
                claims.get(JwtClaims.FIRST_NAME, String.class),
                claims.get(JwtClaims.LAST_NAME, String.class),
                type.getRole());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + type.getRole()))));
    }

    private Optional<String> readBearer(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return Optional.empty();
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
