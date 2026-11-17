package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.entity.User;
import com.petvitaliz.util.Texts;

public record AdminClientResponse(
        @JsonProperty("id_pet") Integer id,
        @JsonProperty("nome") String name,
        @JsonProperty("pet") String kind,
        @JsonProperty("idade") String age,
        @JsonProperty("tutor") String ownerName,
        @JsonProperty("telefone") String phone,
        @JsonProperty("especie") String species,
        String status,
        @JsonProperty("observacoes") String notes
) {

    private static final String NO_NOTES = "Nenhuma observação clínica registrada.";

    public static AdminClientResponse of(Pet pet) {
        User owner = pet.getOwner();
        String notes = pet.getNotes();
        boolean hasNotes = notes != null && !notes.isBlank();
        boolean inTreatment = hasNotes && notes.toLowerCase().contains("tratamento");
        boolean hasOtherSpecies = pet.getOtherSpecies() != null && !pet.getOtherSpecies().isBlank();

        return new AdminClientResponse(
                pet.getId(),
                pet.getName(),
                hasOtherSpecies ? pet.getOtherSpecies() : pet.getSpecies().getDbValue(),
                pet.getAge() + (pet.getAge() == 1 ? " ano" : " anos"),
                Texts.fullName(owner.getFirstName(), owner.getLastName()),
                owner.getPhone(),
                pet.getSpecies().getDisplayName(),
                inTreatment ? "Em tratamento" : "Ativo",
                hasNotes ? notes : NO_NOTES);
    }
}
