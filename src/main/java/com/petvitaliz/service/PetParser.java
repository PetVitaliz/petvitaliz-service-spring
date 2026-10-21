package com.petvitaliz.service;

import com.petvitaliz.enums.PetEspecie;
import com.petvitaliz.enums.PetSexo;
import com.petvitaliz.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;


final class PetParser {

    private PetParser() {
    }

    static String nome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw erro("Nome do pet é obrigatorio");
        }
        return nome.trim();
    }

    static PetEspecie especie(String especie) {
        if (especie == null) {
            throw erro("Espécie inválida");
        }
        try {
            return PetEspecie.valueOf(especie.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw erro("Espécie inválida");
        }
    }

    static String outraEspecie(PetEspecie especie, String outraEspecie) {
        if (especie != PetEspecie.outro) {
            return null;
        }
        if (outraEspecie == null || outraEspecie.isBlank()) {
            throw erro("Informe qual é a espécie do pet");
        }
        String valor = outraEspecie.trim();
        if (valor.length() > 100) {
            throw erro("A outra espécie deve ter no máximo 100 caracteres");
        }
        return valor;
    }

    static PetSexo sexo(String sexo) {
        String valor = sexo == null ? "" : sexo.trim().toLowerCase();
        return (valor.equals("m") || valor.equals("macho")) ? PetSexo.M : PetSexo.F;
    }

    static LocalDate dataNascimento(String data) {
        if (data == null || data.isBlank()) {
            throw erro("Data de nascimento é obrigatorio");
        }
        String texto = data.trim();
        try {
            // Aceita "2020-05-20" e "2020-05-20T00:00:00.000Z" (usa so os 10 primeiros caracteres).
            LocalDate nascimento = LocalDate.parse(texto.substring(0, Math.min(10, texto.length())));
            if (nascimento.isAfter(LocalDate.now())) {
                throw erro("Data de nascimento não pode ser no futuro");
            }
            return nascimento;
        } catch (DateTimeParseException e) {
            throw erro("Data de nascimento inválida (use AAAA-MM-DD)");
        }
    }

    static int idadeOuZero(String idade) {
        if (idade == null || idade.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, (int) Double.parseDouble(idade.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static Double pesoObrigatorio(String peso) {
        Double valor = pesoOpcional(peso);
        if (valor == null) {
            throw erro("Peso é obrigatorio e deve ser um numero");
        }
        return valor;
    }

    static Double pesoOpcional(String peso) {
        if (peso == null || peso.isBlank()) {
            return null;
        }
        try {
            double valor = Double.parseDouble(peso.trim().replace(',', '.'));
            if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 0) {
                throw erro("Peso é obrigatorio e deve ser um numero");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw erro("Peso é obrigatorio e deve ser um numero");
        }
    }

    static String observacoes(String observacoes) {
        if (observacoes == null || observacoes.isBlank()) {
            return null;
        }
        String valor = observacoes.trim();
        if (valor.length() > 1000) {
            throw erro("Observações deve ter no máximo 1000 caracteres");
        }
        return valor;
    }

    private static ApiException erro(String mensagem) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
