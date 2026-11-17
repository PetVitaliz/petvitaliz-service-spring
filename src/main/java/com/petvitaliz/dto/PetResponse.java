package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.PetSex;
import com.petvitaliz.enums.PetSpecies;
import com.petvitaliz.util.DateParser;

public record PetResponse(
        @JsonProperty("id_pet") Integer id,
        @JsonProperty("id_usuario") Integer ownerId,
        @JsonProperty("nome") String name,
        @JsonProperty("especie") PetSpecies species,
        @JsonProperty("outra_especie") String otherSpecies,
        @JsonProperty("sexo") PetSex sex,
        @JsonProperty("data_nascimento") String birthDate,
        @JsonProperty("foto_url") String photoUrl,
        @JsonProperty("idade") Integer age,
        @JsonProperty("peso") Double weight,
        @JsonProperty("observacoes") String notes,
        @JsonProperty("usuario") @JsonInclude(JsonInclude.Include.NON_NULL) OwnerResponse owner
) {

    public static PetResponse of(Pet pet) {
        return withOwner(pet, null);
    }

    public static PetResponse withOwner(Pet pet, OwnerResponse owner) {
        return new PetResponse(
                pet.getId(),
                pet.getOwnerId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getOtherSpecies(),
                pet.getSex(),
                DateParser.toIsoMidnight(pet.getBirthDate()),
                pet.getPhotoUrl(),
                pet.getAge(),
                pet.getWeight(),
                pet.getNotes(),
                owner);
    }
}
