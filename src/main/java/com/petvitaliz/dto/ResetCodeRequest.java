package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ResetCodeRequest(

        @NotBlank(message = "Email é obrigatório")
        String email,

        @JsonProperty("codigo")
        @NotBlank(message = "Codigo invalido")
        String code
) {
}
