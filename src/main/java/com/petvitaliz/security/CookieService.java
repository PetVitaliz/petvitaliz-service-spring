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

    public ResponseCookie criar(String nome, String valor, Duration duracao) {
        return base(nome, valor).maxAge(duracao).build();
    }

    public ResponseCookie limpar(String nome) {
        return base(nome, "").maxAge(Duration.ZERO).build();
    }

    public String comoCabecalho(ResponseCookie cookie, boolean particionado) {
        String cabecalho = cookie.toString();
        return (particionado && secure) ? cabecalho + "; Partitioned" : cabecalho;
    }

    private ResponseCookie.ResponseCookieBuilder base(String nome, String valor) {
        return ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/");
    }
}
