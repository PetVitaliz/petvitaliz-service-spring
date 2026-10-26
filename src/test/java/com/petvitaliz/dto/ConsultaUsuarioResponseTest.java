package com.petvitaliz.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petvitaliz.entity.Consulta;
import com.petvitaliz.entity.Funcionario;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.ConsultaStatus;
import com.petvitaliz.enums.FuncionarioEspecialidade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultaUsuarioResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializaComOsNomesEOsFormatosDaApiAntiga() {
        Pet pet = new Pet();
        pet.setId(5);
        pet.setNome("Rex");

        Funcionario funcionario = new Funcionario();
        funcionario.setId(3);
        funcionario.setNome("Ana");
        funcionario.setEspecialidade(FuncionarioEspecialidade.veterinario);

        Consulta consulta = new Consulta();
        consulta.setId(9);
        consulta.setPet(pet);
        consulta.setFuncionario(funcionario);
        consulta.setDataConsulta(LocalDate.of(2026, 10, 20));
        consulta.setHoraInicio("09:00");
        consulta.setHoraFim("10:00");
        consulta.setStatus(ConsultaStatus.EM_ESPERA);
        consulta.setObservacoes("");

        JsonNode json = mapper.valueToTree(ConsultaUsuarioResponse.de(consulta));

        assertThat(json.get("id_consulta").asInt()).isEqualTo(9);
        assertThat(json.get("id_pet").asInt()).isEqualTo(5);
        assertThat(json.get("id_funcionario").asInt()).isEqualTo(3);
        assertThat(json.get("data_consulta").asText()).isEqualTo("2026-10-20T00:00:00.000Z");
        assertThat(json.get("hora_inicio").asText()).isEqualTo("09:00");
        assertThat(json.get("status").asText()).isEqualTo("em_espera");
        assertThat(json.get("pet").get("nome").asText()).isEqualTo("Rex");
        assertThat(json.get("funcionario").get("nome").asText()).isEqualTo("Ana");
        assertThat(json.get("funcionario").get("especialidade").asText()).isEqualTo("veterinario");
    }
}
