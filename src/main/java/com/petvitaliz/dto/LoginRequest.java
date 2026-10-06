package com.petvitaliz.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Campo email é obrigatorio")
        String email,

        @NotBlank(message = "Campo senha é obrigatorio")
        String senha
) {
}
