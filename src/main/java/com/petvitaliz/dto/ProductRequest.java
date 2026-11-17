package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record ProductRequest(
        @JsonProperty("nome") String name,
        @JsonProperty("descricao") String description,
        @JsonProperty("beneficios") String benefits,
        @JsonProperty("preco") JsonNode price
) {
}
