package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Consulta;
import com.petvitaliz.enums.ConsultaStatus;

public record ConsultaUsuarioResponse(
        @JsonProperty("id_consulta") Integer idConsulta,
        @JsonProperty("id_pet") Integer idPet,
        @JsonProperty("id_funcionario") Integer idFuncionario,
        @JsonProperty("data_consulta") String dataConsulta,
        @JsonProperty("hora_inicio") String horaInicio,
        @JsonProperty("hora_fim") String horaFim,
        ConsultaStatus status,
        String observacoes,
        PetResumo pet,
        FuncionarioResumo funcionario
) {

    public record PetResumo(String nome) {
    }

    public record FuncionarioResumo(String nome, String especialidade) {
    }

    public static ConsultaUsuarioResponse de(Consulta consulta) {
        return new ConsultaUsuarioResponse(
                consulta.getId(),
                consulta.getPet().getId(),
                consulta.getFuncionario().getId(),
                consulta.getDataConsulta() + "T00:00:00.000Z",
                consulta.getHoraInicio(),
                consulta.getHoraFim(),
                consulta.getStatus(),
                consulta.getObservacoes(),
                new PetResumo(consulta.getPet().getNome()),
                new FuncionarioResumo(
                        consulta.getFuncionario().getNome(),
                        consulta.getFuncionario().getEspecialidade().name()));
    }
}
