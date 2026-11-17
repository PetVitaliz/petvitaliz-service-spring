package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Campo email é obrigatorio")
        String email,

        @JsonProperty("senha")
        @NotBlank(message = "Campo senha é obrigatorio")
        String password
) {
}
