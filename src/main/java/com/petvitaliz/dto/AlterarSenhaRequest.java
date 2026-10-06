package com.petvitaliz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(

        @NotNull(message = "A senha deve ter pelo menos 6 caracteres e não pode estar vazia")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres e não pode estar vazia")
        String senha1,

        @NotNull(message = "A senha deve ter pelo menos 6 caracteres e não pode estar vazia")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres e não pode estar vazia")
        String senha2
) {
}
