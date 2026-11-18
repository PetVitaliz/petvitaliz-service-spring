package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Subscription;

public record PetPlanResponse(
        @JsonProperty("id_pet") Integer petId,
        @JsonProperty("produto") ProductResponse product
) {

    public static PetPlanResponse of(Subscription subscription) {
        return new PetPlanResponse(subscription.getPetId(), ProductResponse.of(subscription.getProduct()));
    }
}
