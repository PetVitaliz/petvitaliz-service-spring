package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Pet;

public record PetResponse(
        @JsonProperty("id_pet") Integer idPet,
        @JsonProperty("id_usuario") Integer idUsuario,
        String nome,
        String especie,
        @JsonProperty("outra_especie") String outraEspecie,
        String sexo,
        @JsonProperty("data_nascimento") String dataNascimento,
        @JsonProperty("foto_url") String fotoUrl,
        Integer idade,
        Double peso,
        String observacoes
) {

    public static PetResponse de(Pet pet, Integer idUsuario) {
        return new PetResponse(
                pet.getId(),
                idUsuario,
                pet.getNome(),
                pet.getEspecie().name(),
                pet.getOutraEspecie(),
                pet.getSexo().name(),
                pet.getDataNascimento() + "T00:00:00.000Z",
                pet.getFotoUrl(),
                pet.getIdade(),
                pet.getPeso(),
                pet.getObservacoes());
    }
}
