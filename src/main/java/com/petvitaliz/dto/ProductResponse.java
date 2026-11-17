package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Product;
import com.petvitaliz.util.Texts;

public record ProductResponse(
        @JsonProperty("id_produto") @JsonInclude(JsonInclude.Include.NON_NULL) Integer id,
        @JsonProperty("nome") String name,
        @JsonProperty("descricao") String description,
        @JsonProperty("beneficios") String benefits,
        @JsonProperty("preco") String price
) {

    public static ProductResponse of(Product product) {
        return build(product, null);
    }

    public static ProductResponse withId(Product product) {
        return build(product, product.getId());
    }

    private static ProductResponse build(Product product, Integer id) {
        return new ProductResponse(
                id,
                product.getName(),
                product.getDescription(),
                product.getBenefits(),
                Texts.plainNumber(product.getPrice()));
    }
}
