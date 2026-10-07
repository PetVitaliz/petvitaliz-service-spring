package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PerfilResponse(
        String nome,
        String sobrenome,
        String email,
        @JsonProperty("CPF") String cpf
) {
}
