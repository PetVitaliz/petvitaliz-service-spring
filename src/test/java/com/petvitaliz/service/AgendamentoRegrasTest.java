package com.petvitaliz.service;

import com.petvitaliz.enums.FuncionarioEspecialidade;
import com.petvitaliz.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgendamentoRegrasTest {

    @Test
    void servicoAceitaTosadorEVeterinarioComEspacosEMaiusculas() {
        assertThat(AgendamentoRegras.servico(" Tosador ")).isEqualTo(FuncionarioEspecialidade.tosador);
        assertThat(AgendamentoRegras.servico("VETERINARIO")).isEqualTo(FuncionarioEspecialidade.veterinario);
    }

    @Test
    void servicoRejeitaOutrasEspecialidades() {
        assertThatThrownBy(() -> AgendamentoRegras.servico("financeiro")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> AgendamentoRegras.servico(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void horaEhNormalizadaParaDoisDigitos() {
        assertThat(AgendamentoRegras.normalizarHora("9:00")).isEqualTo("09:00");
        assertThat(AgendamentoRegras.normalizarHora(" 14:30 ")).isEqualTo("14:30");
        assertThat(AgendamentoRegras.normalizarHora("23:59")).isEqualTo("23:59");
    }

    @Test
    void horaInvalidaEhRejeitada() {
        for (String invalida : new String[]{"24:00", "12:60", "abc", "", "12", "12:5"}) {
            assertThatThrownBy(() -> AgendamentoRegras.normalizarHora(invalida))
                    .isInstanceOf(ApiException.class);
        }
        assertThatThrownBy(() -> AgendamentoRegras.normalizarHora(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void horaFimSomaUmaHoraEVoltaAposMeiaNoite() {
        assertThat(AgendamentoRegras.horaFim("09:00")).isEqualTo("10:00");
        assertThat(AgendamentoRegras.horaFim("23:30")).isEqualTo("00:30");
    }

    @Test
    void dataAceitaFormatoSimplesEIso() {
        assertThat(AgendamentoRegras.lerData("2026-10-20")).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(AgendamentoRegras.lerData("2026-10-20T00:00:00.000Z")).isEqualTo(LocalDate.of(2026, 10, 20));
    }

    @Test
    void dataInvalidaEhRejeitada() {
        for (String invalida : new String[]{"2026-02-30", "20/10/2026", "", "amanha"}) {
            assertThatThrownBy(() -> AgendamentoRegras.lerData(invalida)).isInstanceOf(ApiException.class);
        }
        assertThatThrownBy(() -> AgendamentoRegras.lerData(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void horarioQueJaPassouEhRejeitado() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 20, 10, 0);

        assertThatThrownBy(() -> AgendamentoRegras.exigirFuturo(LocalDate.of(2026, 10, 20), "09:59", agora))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> AgendamentoRegras.exigirFuturo(LocalDate.of(2026, 10, 19), "23:00", agora))
                .isInstanceOf(ApiException.class);
        assertThatCode(() -> AgendamentoRegras.exigirFuturo(LocalDate.of(2026, 10, 20), "10:00", agora))
                .doesNotThrowAnyException();
        assertThatCode(() -> AgendamentoRegras.exigirFuturo(LocalDate.of(2026, 10, 21), "08:00", agora))
                .doesNotThrowAnyException();
    }

    @Test
    void observacoesTratamVazioEExcesso() {
        assertThat(AgendamentoRegras.observacoes(null)).isEmpty();
        assertThat(AgendamentoRegras.observacoes("   ")).isEmpty();
        assertThat(AgendamentoRegras.observacoes(" vacina ")).isEqualTo("vacina");
        String longa = "a".repeat(501);
        assertThatThrownBy(() -> AgendamentoRegras.observacoes(longa)).isInstanceOf(ApiException.class);
    }
}
