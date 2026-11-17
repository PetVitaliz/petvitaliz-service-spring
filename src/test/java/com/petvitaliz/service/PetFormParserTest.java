package com.petvitaliz.service;

import com.petvitaliz.enums.PetSex;
import com.petvitaliz.enums.PetSpecies;
import com.petvitaliz.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PetFormParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    @Test
    void maleSexBecomesMAndAnythingElseBecomesF() {
        assertThat(PetFormParser.sex("Macho")).isEqualTo(PetSex.M);
        assertThat(PetFormParser.sex(" m ")).isEqualTo(PetSex.M);
        assertThat(PetFormParser.sex("femea")).isEqualTo(PetSex.F);
        assertThat(PetFormParser.sex(null)).isEqualTo(PetSex.F);
    }

    @Test
    void speciesIgnoresCaseAndSpacesAndRejectsUnknownValues() {
        assertThat(PetFormParser.species("  Cachorro ")).isEqualTo(PetSpecies.DOG);
        assertThatThrownBy(() -> PetFormParser.species("dragao")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetFormParser.species(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void otherSpeciesIsOnlyKeptWhenSpeciesIsOther() {
        assertThat(PetFormParser.otherSpecies(PetSpecies.CAT, "anything")).isNull();
        assertThat(PetFormParser.otherSpecies(PetSpecies.OTHER, " Hamster ")).isEqualTo("Hamster");
        assertThatThrownBy(() -> PetFormParser.otherSpecies(PetSpecies.OTHER, " ")).isInstanceOf(ApiException.class);
    }

    @Test
    void birthDateAcceptsPlainAndIsoFormatsAndRejectsFutureOrInvalidValues() {
        assertThat(PetFormParser.birthDate("2020-05-20", TODAY)).isEqualTo(LocalDate.of(2020, 5, 20));
        assertThat(PetFormParser.birthDate("2020-05-20T00:00:00.000Z", TODAY)).isEqualTo(LocalDate.of(2020, 5, 20));
        assertThatThrownBy(() -> PetFormParser.birthDate("20/05/2020", TODAY)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetFormParser.birthDate("", TODAY)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetFormParser.birthDate(TODAY.plusDays(1).toString(), TODAY))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void weightAcceptsCommaAndIsRequiredOnCreation() {
        assertThat(PetFormParser.requiredWeight("4,5")).isEqualTo(4.5);
        assertThat(PetFormParser.optionalWeight("")).isNull();
        assertThatThrownBy(() -> PetFormParser.requiredWeight(null)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetFormParser.requiredWeight("abc")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> PetFormParser.requiredWeight("-3")).isInstanceOf(ApiException.class);
    }

    @Test
    void invalidOrMissingAgeBecomesZero() {
        assertThat(PetFormParser.ageOrZero("3")).isEqualTo(3);
        assertThat(PetFormParser.ageOrZero("")).isZero();
        assertThat(PetFormParser.ageOrZero("abc")).isZero();
        assertThat(PetFormParser.ageOrZero("-2")).isZero();
    }
}
