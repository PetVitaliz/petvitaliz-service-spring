package com.petvitaliz.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum ConsultaStatus {
    EM_ESPERA("em espera", "em_espera"),
    EM_ANDAMENTO("em andamento", "em_andamento"),
    FINALIZADO("finalizado", "finalizado");

    private final String valorBanco;
    private final String valorApi;

    ConsultaStatus(String valorBanco, String valorApi) {
        this.valorBanco = valorBanco;
        this.valorApi = valorApi;
    }

    public String getValorBanco() {
        return valorBanco;
    }

    @JsonValue
    public String getValorApi() {
        return valorApi;
    }

    @JsonCreator
    public static ConsultaStatus fromApi(String valor) {
        for (ConsultaStatus s : values()) {
            if (s.valorApi.equals(valor)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status invalido: " + valor);
    }

    public static ConsultaStatus fromBanco(String valor) {
        for (ConsultaStatus s : values()) {
            if (s.valorBanco.equals(valor)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status invalido no banco: " + valor);
    }
}
