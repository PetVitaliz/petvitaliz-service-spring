package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.util.Texts;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public record PatientResponse(
        @JsonProperty("id_pet") Integer id,
        @JsonProperty("id_usuario") Integer ownerId,
        @JsonProperty("nome") String name,
        @JsonProperty("especie") String species,
        @JsonProperty("detalheEspecie") String speciesDetail,
        @JsonProperty("idade") Integer age,
        @JsonProperty("idadeTexto") String ageText,
        @JsonProperty("sexo") String sex,
        @JsonProperty("peso") String weight,
        String status,
        @JsonProperty("tutor") String ownerName,
        @JsonProperty("prontuario") String record,
        @JsonProperty("plano") String plan,
        @JsonProperty("consultasDoMes") List<MonthAppointmentResponse> monthAppointments
) {

    private static final String NO_NOTES =
            "Nenhuma observação clínica registrada para o paciente até o momento.";

    public static PatientResponse of(Pet pet, LocalDate today, String plan,
                                     List<MonthAppointmentResponse> monthAppointments) {
        Double weight = pet.getWeight();
        boolean weighed = weight != null && weight > 0;
        String notes = pet.getNotes();

        return new PatientResponse(
                pet.getId(),
                pet.getOwnerId(),
                pet.getName(),
                pet.getSpecies().getDisplayName(),
                pet.getOtherSpecies() == null ? null : pet.getOtherSpecies().trim(),
                pet.getAge(),
                ageText(pet.getAge(), pet.getBirthDate(), today),
                pet.getSex().getDisplayName(),
                weighed ? Texts.plainNumber(weight) + " kg" : "Não pesado",
                weighed ? "Ativo" : "Em Tratamento",
                Texts.fullName(pet.getOwner().getFirstName(), pet.getOwner().getLastName()),
                notes == null || notes.isBlank() ? NO_NOTES : notes,
                plan,
                monthAppointments);
    }

    static String ageText(Integer storedAge, LocalDate birthDate, LocalDate today) {
        if (storedAge != null && storedAge == 0) {
            return "Menos de 1 ano";
        }
        Period lived = birthDate == null ? Period.ZERO : Period.between(birthDate, today);
        if (birthDate != null && lived.getYears() == 0) {
            return lived.getMonths() + " meses";
        }
        if (lived.getMonths() > 0) {
            return lived.getYears() + " anos e " + lived.getMonths() + " m";
        }
        return storedAge + " anos";
    }
}
