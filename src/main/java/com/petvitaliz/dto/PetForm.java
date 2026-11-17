package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.BindParam;

public record PetForm(
        @JsonProperty("nome") @BindParam("nome") String name,
        @JsonProperty("especie") @BindParam("especie") String species,
        @JsonProperty("outra_especie") @BindParam("outra_especie") String otherSpecies,
        @JsonProperty("sexo") @BindParam("sexo") String sex,
        @JsonProperty("data_nascimento") @BindParam("data_nascimento") String birthDate,
        @JsonProperty("idade") @BindParam("idade") String age,
        @JsonProperty("peso") @BindParam("peso") String weight,
        @JsonProperty("observacoes") @BindParam("observacoes") String notes
) {
}
