package com.petvitaliz.dto;

import jakarta.validation.constraints.NotBlank;

public record PedirResetRequest(

        @NotBlank(message = "Email é obrigatório")
        String email
) {
}
