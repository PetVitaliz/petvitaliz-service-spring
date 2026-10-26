package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AgendamentoRequest(

        @NotBlank(message = "Todos os campos são obrigatorios")
        String servico,

        @JsonProperty("id_pet")
        @NotNull(message = "Todos os campos são obrigatorios")
        Integer idPet,

        @JsonProperty("data_consulta")
        @NotBlank(message = "Todos os campos são obrigatorios")
        String dataConsulta,

        @JsonProperty("hora_inicio")
        @NotBlank(message = "Todos os campos são obrigatorios")
        String horaInicio,

        String observacoes
) {
}
