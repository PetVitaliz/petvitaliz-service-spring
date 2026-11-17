package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(

        @JsonProperty("servico")
        @NotBlank(message = AppointmentRequest.REQUIRED)
        String service,

        @JsonProperty("id_pet")
        @NotNull(message = AppointmentRequest.REQUIRED)
        Integer petId,

        @JsonProperty("data_consulta")
        @NotBlank(message = AppointmentRequest.REQUIRED)
        String date,

        @JsonProperty("hora_inicio")
        @NotBlank(message = AppointmentRequest.REQUIRED)
        String startTime,

        @JsonProperty("observacoes")
        String notes
) {
    static final String REQUIRED = "Todos os campos são obrigatorios";
}
