package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @JsonProperty("nome")
        @NotNull(message = "Nome é obrigatório e deve ter pelo menos 2 caracteres")
        @Size(min = 2, message = "Nome é obrigatório e deve ter pelo menos 2 caracteres")
        String firstName,

        @JsonProperty("sobrenome")
        @NotNull(message = "Sobrenome é obrigatório e deve ter pelo menos 3 caracteres")
        @Size(min = 3, message = "Sobrenome é obrigatório e deve ter pelo menos 3 caracteres")
        String lastName,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @JsonProperty("senha")
        @NotNull(message = "Senha é obrigatória e deve ter pelo menos 6 caracteres")
        @Size(min = 6, message = "Senha é obrigatória e deve ter pelo menos 6 caracteres")
        String password,

        @JsonProperty("CPF")
        @NotNull(message = "CPF é obrigatório e deve ter 11 dígitos")
        @Size(min = 11, max = 11, message = "CPF é obrigatório e deve ter 11 dígitos")
        String cpf,

        @JsonProperty("telefone")
        @NotNull(message = "Telefone é obrigatório e deve ter 11 dígitos")
        @Size(min = 11, max = 11, message = "Telefone é obrigatório e deve ter 11 dígitos")
        String phone,

        @JsonProperty("genero")
        @NotNull(message = "Gênero é obrigatório e deve ser 'f', 'm' ou 'o'")
        @Pattern(regexp = "(?i)[mfo]", message = "Gênero é obrigatório e deve ser 'f', 'm' ou 'o'")
        String gender,

        @JsonProperty("data_nascimento")
        @NotBlank(message = "Data de nascimento é obrigatória")
        String birthDate
) {
}
