package com.petvitaliz.controller;

import com.petvitaliz.security.CookieService;
import com.petvitaliz.security.TokenType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Map;

final class LogoutResponses {

    private LogoutResponses() {
    }

    static ResponseEntity<Map<String, String>> clearing(CookieService cookieService, TokenType type) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.clear(type))
                .body(Map.of("message", "Logout realizado com sucesso"));
    }
}
