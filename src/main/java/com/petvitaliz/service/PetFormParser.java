package com.petvitaliz.service;

import com.petvitaliz.enums.DbEnum;
import com.petvitaliz.enums.PetSex;
import com.petvitaliz.enums.PetSpecies;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.util.DateParser;

import java.time.LocalDate;

final class PetFormParser {

    private static final int MAX_OTHER_SPECIES_LENGTH = 100;
    private static final int MAX_NOTES_LENGTH = 1000;
    private static final String INVALID_WEIGHT = "Peso é obrigatorio e deve ser um numero";

    private PetFormParser() {
    }

    static String name(String name) {
        if (isBlank(name)) {
            throw ApiException.badRequest("Nome do pet é obrigatorio");
        }
        return name.trim();
    }

    static PetSpecies species(String species) {
        return DbEnum.find(PetSpecies.class, species)
                .orElseThrow(() -> ApiException.badRequest("Espécie inválida"));
    }

    static String otherSpecies(PetSpecies species, String otherSpecies) {
        if (species != PetSpecies.OTHER) {
            return null;
        }
        if (isBlank(otherSpecies)) {
            throw ApiException.badRequest("Informe qual é a espécie do pet");
        }
        String value = otherSpecies.trim();
        if (value.length() > MAX_OTHER_SPECIES_LENGTH) {
            throw ApiException.badRequest("A outra espécie deve ter no máximo 100 caracteres");
        }
        return value;
    }

    static PetSex sex(String sex) {
        String value = sex == null ? "" : sex.trim().toLowerCase();
        return value.equals("m") || value.equals("macho") ? PetSex.M : PetSex.F;
    }

    static LocalDate birthDate(String birthDate, LocalDate today) {
        if (isBlank(birthDate)) {
            throw ApiException.badRequest("Data de nascimento é obrigatorio");
        }
        LocalDate parsed = DateParser.parse(birthDate).orElseThrow(() ->
                ApiException.badRequest("Data de nascimento inválida (use AAAA-MM-DD)"));
        if (parsed.isAfter(today)) {
            throw ApiException.badRequest("Data de nascimento não pode ser no futuro");
        }
        return parsed;
    }

    static int ageOrZero(String age) {
        if (isBlank(age)) {
            return 0;
        }
        try {
            return Math.max(0, (int) Double.parseDouble(age.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static Double requiredWeight(String weight) {
        Double value = optionalWeight(weight);
        if (value == null) {
            throw ApiException.badRequest(INVALID_WEIGHT);
        }
        return value;
    }

    static Double optionalWeight(String weight) {
        if (isBlank(weight)) {
            return null;
        }
        try {
            double value = Double.parseDouble(weight.trim().replace(',', '.'));
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
                throw ApiException.badRequest(INVALID_WEIGHT);
            }
            return value;
        } catch (NumberFormatException e) {
            throw ApiException.badRequest(INVALID_WEIGHT);
        }
    }

    static String notes(String notes) {
        if (isBlank(notes)) {
            return null;
        }
        String value = notes.trim();
        if (value.length() > MAX_NOTES_LENGTH) {
            throw ApiException.badRequest("Observações deve ter no máximo 1000 caracteres");
        }
        return value;
    }

    static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
