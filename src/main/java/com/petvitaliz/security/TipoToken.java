package com.petvitaliz.security;

import java.time.Duration;

public enum TipoToken {

    USUARIO("token", "USUARIO", Duration.ofDays(3),
            "Acesso negado, faça login",
            "Sessão expirada, faça login novamente"),

    ADMINISTRADOR("token_adm", "ADMINISTRADOR", Duration.ofDays(3),
            "Acesso negado, faça login como administrador",
            "Sessão expirada, faça login novamente"),

    FUNCIONARIO("token_funcionario", "FUNCIONARIO", Duration.ofHours(1),
            "Acesso negado, faça login como funcionario",
            "Sessão expirada, faça login novamente"),

    RESET("token_reset", "RESET", Duration.ofMinutes(9),
            "Acesso negado",
            "Sessão expirada, peça um codigo novamente");

    private final String nomeCookie;
    private final String role;
    private final Duration validade;
    private final String mensagemNaoAutenticado;
    private final String mensagemSessaoExpirada;

    TipoToken(String nomeCookie, String role, Duration validade,
              String mensagemNaoAutenticado, String mensagemSessaoExpirada) {
        this.nomeCookie = nomeCookie;
        this.role = role;
        this.validade = validade;
        this.mensagemNaoAutenticado = mensagemNaoAutenticado;
        this.mensagemSessaoExpirada = mensagemSessaoExpirada;
    }

    public String getNomeCookie() {
        return nomeCookie;
    }

    public String getRole() {
        return role;
    }

    public Duration getValidade() {
        return validade;
    }

    public String getMensagemNaoAutenticado() {
        return mensagemNaoAutenticado;
    }

    public String getMensagemSessaoExpirada() {
        return mensagemSessaoExpirada;
    }

    public static TipoToken paraCaminho(String caminho) {
        if (caminho.equals("/user/login/alterar-senha")) {
            return RESET;
        }
        if (caminho.equals("/adm") || caminho.startsWith("/adm/")) {
            return ADMINISTRADOR;
        }
        if (caminho.equals("/funcionario") || caminho.startsWith("/funcionario/")) {
            return FUNCIONARIO;
        }
        return USUARIO;
    }
}
