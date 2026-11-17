package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.PetSpecies;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PetSummaryResponse(
        @JsonProperty("nome") String name,
        @JsonProperty("idade") Integer age,
        @JsonProperty("especie") PetSpecies species,
        @JsonProperty("usuario") OwnerResponse owner
) {

    public static PetSummaryResponse nameOf(Pet pet) {
        return new PetSummaryResponse(pet.getName(), null, null, null);
    }

    public static PetSummaryResponse of(Pet pet) {
        return new PetSummaryResponse(
                pet.getName(), pet.getAge(), pet.getSpecies(), OwnerResponse.nameOf(pet.getOwner()));
    }
}
