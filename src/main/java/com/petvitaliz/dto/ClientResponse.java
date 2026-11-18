package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.entity.User;
import com.petvitaliz.enums.PetSex;
import com.petvitaliz.enums.PetSpecies;
import com.petvitaliz.util.DateParser;
import com.petvitaliz.util.Texts;

import java.util.List;
import java.util.Map;

public record ClientResponse(
        @JsonProperty("id_usuario") Integer id,
        @JsonProperty("nome") String name,
        String cpf,
        @JsonProperty("telefone") String phone,
        String email,
        @JsonProperty("pets") String petCount,
        @JsonProperty("petsLista") List<String> petNames,
        @JsonProperty("petsDetalhesLista") List<PetDetails> petDetails,
        String status,
        @JsonProperty("ultimaVisita") String lastVisit
) {

    public record PetDetails(
            @JsonProperty("id_pet") Integer id,
            @JsonProperty("nome") String name,
            @JsonProperty("especie") PetSpecies species,
            @JsonProperty("sexo") PetSex sex,
            @JsonProperty("peso") Double weight,
            @JsonProperty("data_nascimento") String birthDate,
            @JsonProperty("plano") String plan
    ) {

        static PetDetails of(Pet pet, String plan) {
            return new PetDetails(
                    pet.getId(),
                    pet.getName(),
                    pet.getSpecies(),
                    pet.getSex(),
                    pet.getWeight(),
                    DateParser.toIsoMidnight(pet.getBirthDate()),
                    plan);
        }
    }

    public static ClientResponse of(User user, Map<Integer, String> plansByPet) {
        List<Pet> pets = user.getPets();
        return new ClientResponse(
                user.getId(),
                Texts.fullName(user.getFirstName(), user.getLastName()),
                maskCpf(user.getCpf()),
                user.getPhone(),
                user.getEmail(),
                String.format("%02d", pets.size()),
                pets.stream().map(Pet::getName).toList(),
                pets.stream().map(pet -> PetDetails.of(pet, plansByPet.get(pet.getId()))).toList(),
                "ATIVO",
                "Consultar histórico");
    }

    static String maskCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return "Não informado";
        }
        return cpf.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
