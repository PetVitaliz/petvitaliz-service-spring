package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewPasswordRequest(

        @JsonProperty("senha1")
        @NotNull(message = NewPasswordRequest.INVALID)
        @Size(min = 6, message = NewPasswordRequest.INVALID)
        String password,

        @JsonProperty("senha2")
        @NotNull(message = NewPasswordRequest.INVALID)
        @Size(min = 6, message = NewPasswordRequest.INVALID)
        String confirmation
) {
    static final String INVALID = "A senha deve ter pelo menos 6 caracteres e não pode estar vazia";
}
