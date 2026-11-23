package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(

        @JsonProperty("nome")
        @NotNull(message = "Nome é obrigatório e deve ter pelo menos 2 caracteres")
        @Size(min = 2, message = "Nome é obrigatório e deve ter pelo menos 2 caracteres")
        String firstName,

        @JsonProperty("sobrenome")
        String lastName,

        @JsonProperty("telefone")
        @Pattern(regexp = "\\D*(\\d\\D*){10,11}", message = "Telefone deve ter DDD + número (10 ou 11 dígitos)")
        String phone,

        @JsonProperty("data_nascimento")
        String birthDate
) {
}
