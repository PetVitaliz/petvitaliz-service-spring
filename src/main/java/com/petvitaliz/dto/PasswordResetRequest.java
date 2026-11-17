package com.petvitaliz.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(

        @NotBlank(message = "Email é obrigatório")
        String email
) {
}
