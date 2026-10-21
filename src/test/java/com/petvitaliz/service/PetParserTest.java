package com.petvitaliz.service;

import com.petvitaliz.enums.PetEspecie;
import com.petvitaliz.enums.PetSexo;
import com.petvitaliz.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PetParserTest {

    @Test
    void sexoMachoViraM_eQualquerOutroViraF() {
        assertThat(PetParser.sexo("Macho")).isEqualTo(PetSexo.M);
        assertThat(PetParser.sexo(" m ")).isEqualTo(PetSexo.M);
        assertThat(PetParser.sexo("femea")).isEqualTo(PetSexo.F);
        assertThat(PetParser.sexo(null)).isEqualTo(PetSexo.F);
    }

    @Test
    void especieAceitaMaiusculaEEspacos_eRejeitaDesconhecida() {
        assertThat(PetParser.especie("  Cachorro ")).isEqualTo(PetEspecie.cachorro);
        assertThatThrownBy(() -> PetParser.especie("dragao")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetParser.especie(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void outraEspecieSoVaiParaOBancoQuandoEspecieEOutro() {
        assertThat(PetParser.outraEspecie(PetEspecie.gato, "qualquer")).isNull();
        assertThat(PetParser.outraEspecie(PetEspecie.outro, " Hamster ")).isEqualTo("Hamster");
        assertThatThrownBy(() -> PetParser.outraEspecie(PetEspecie.outro, " "))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void dataAceitaFormatoSimplesEIso_eRejeitaFuturoEInvalida() {
        assertThat(PetParser.dataNascimento("2020-05-20")).isEqualTo(LocalDate.of(2020, 5, 20));
        assertThat(PetParser.dataNascimento("2020-05-20T00:00:00.000Z")).isEqualTo(LocalDate.of(2020, 5, 20));
        assertThatThrownBy(() -> PetParser.dataNascimento("20/05/2020")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetParser.dataNascimento("")).isInstanceOf(ApiException.class);
        String futuro = LocalDate.now().plusDays(2).toString();
        assertThatThrownBy(() -> PetParser.dataNascimento(futuro)).isInstanceOf(ApiException.class);
    }

    @Test
    void pesoAceitaVirgula_eObrigatorioNoCadastro() {
        assertThat(PetParser.pesoObrigatorio("4,5")).isEqualTo(4.5);
        assertThat(PetParser.pesoOpcional("")).isNull();
        assertThatThrownBy(() -> PetParser.pesoObrigatorio(null)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetParser.pesoObrigatorio("abc")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetParser.pesoObrigatorio("-3")).isInstanceOf(ApiException.class);
    }

    @Test
    void idadeInvalidaOuAusenteViraZero() {
        assertThat(PetParser.idadeOuZero("3")).isEqualTo(3);
        assertThat(PetParser.idadeOuZero("")).isZero();
        assertThat(PetParser.idadeOuZero("abc")).isZero();
        assertThat(PetParser.idadeOuZero("-2")).isZero();
    }
}
