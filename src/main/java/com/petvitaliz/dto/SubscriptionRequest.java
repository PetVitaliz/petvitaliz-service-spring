package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(

        @JsonProperty("id_produto")
        @NotNull(message = "Id inválido")
        @Min(value = 1, message = "Id inválido")
        Integer productId,

        @JsonProperty("id_pet")
        @NotNull(message = "Escolha o pet que vai receber o plano")
        @Min(value = 1, message = "Pet inválido")
        Integer petId
) {
}
