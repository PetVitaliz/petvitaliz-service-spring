package com.petvitaliz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContatoRequest(

        @NotBlank(message = "email é obrigatorio")
        @Email(message = "email inválido")
        String email,

        @NotNull(message = "mensagem é obrigatorio e precisa ter pelo menos 6 caracteres")
        @Size(min = 6, message = "mensagem é obrigatorio e precisa ter pelo menos 6 caracteres")
        @Size(max = 2000, message = "mensagem deve ter no máximo 2000 caracteres")
        String mensagem
) {
}
