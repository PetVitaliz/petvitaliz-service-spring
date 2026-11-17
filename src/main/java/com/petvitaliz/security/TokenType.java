package com.petvitaliz.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum TokenType {

    USER("token", Duration.ofDays(3),
            "Acesso negado, faça login",
            "Sessão expirada, faça login novamente"),

    ADMIN("token_adm", Duration.ofDays(3),
            "Acesso negado, faça login como administrador",
            "Sessão expirada, faça login novamente"),

    EMPLOYEE("token_funcionario", Duration.ofHours(1),
            "Acesso negado, faça login como funcionario",
            "Sessão expirada, faça login novamente"),

    PASSWORD_RESET("token_reset", Duration.ofMinutes(9),
            "Acesso negado",
            "Sessão expirada, peça um codigo novamente");

    public static final String PASSWORD_RESET_PATH = "/user/login/alterar-senha";

    private final String cookieName;
    private final Duration lifetime;
    private final String unauthenticatedMessage;
    private final String expiredMessage;

    public String getRole() {
        return name();
    }

    public static TokenType forPath(String path) {
        if (path.equals(PASSWORD_RESET_PATH)) {
            return PASSWORD_RESET;
        }
        if (path.equals("/adm") || path.startsWith("/adm/")) {
            return ADMIN;
        }
        if (path.equals("/funcionario") || path.startsWith("/funcionario/")) {
            return EMPLOYEE;
        }
        return USER;
    }
}
