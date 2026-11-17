package com.petvitaliz.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieService {

    private final boolean secure;
    private final String sameSite;

    public CookieService(@Value("${app.cookies.secure:true}") boolean secure,
                         @Value("${app.cookies.same-site:None}") String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public String issue(TokenType type, String token) {
        return header(type, token, type.getLifetime());
    }

    public String clear(TokenType type) {
        return header(type, "", Duration.ZERO);
    }

    private String header(TokenType type, String value, Duration maxAge) {
        String cookie = ResponseCookie.from(type.getCookieName(), value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString();
        boolean partitioned = secure && type == TokenType.PASSWORD_RESET;
        return partitioned ? cookie + "; Partitioned" : cookie;
    }
}
