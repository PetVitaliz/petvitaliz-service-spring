package com.petvitaliz.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarCodigoRequest(

        @NotBlank(message = "Email é obrigatório")
        String email,

        @NotBlank(message = "Codigo invalido")
        String codigo
) {
}
