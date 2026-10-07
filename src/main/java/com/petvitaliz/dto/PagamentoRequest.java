package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PagamentoRequest(

        @JsonProperty("id_produto")
        @NotNull(message = "Id inválido")
        @Min(value = 1, message = "Id inválido")
        Integer idProduto
) {
}
