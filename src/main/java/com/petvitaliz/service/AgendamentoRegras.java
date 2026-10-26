package com.petvitaliz.service;

import com.petvitaliz.enums.FuncionarioEspecialidade;
import com.petvitaliz.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AgendamentoRegras {

    private static final Pattern HORA = Pattern.compile("^([01]?\\d|2[0-3]):([0-5]\\d)$");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private AgendamentoRegras() {
    }

    static FuncionarioEspecialidade servico(String servico) {
        String valor = servico == null ? "" : servico.trim().toLowerCase();
        return switch (valor) {
            case "tosador" -> FuncionarioEspecialidade.tosador;
            case "veterinario" -> FuncionarioEspecialidade.veterinario;
            default -> throw erro("no momento so temos 'tosador' e 'veterinario'");
        };
    }

    static LocalDate lerData(String texto) {
        String data = texto == null ? "" : texto.trim();
        try {
            return LocalDate.parse(data.substring(0, Math.min(10, data.length())));
        } catch (DateTimeParseException e) {
            throw erro("Data da consulta inválida (use AAAA-MM-DD)");
        }
    }

    static String normalizarHora(String texto) {
        String hora = texto == null ? "" : texto.trim();
        Matcher matcher = HORA.matcher(hora);
        if (!matcher.matches()) {
            throw erro("Hora de início inválida (use HH:mm)");
        }
        return String.format("%02d:%s", Integer.parseInt(matcher.group(1)), matcher.group(2));
    }

    static String horaFim(String horaInicio) {
        return LocalTime.parse(horaInicio).plusHours(1).format(FORMATO_HORA);
    }

    static void exigirFuturo(LocalDate data, String horaInicio, LocalDateTime agora) {
        if (LocalDateTime.of(data, LocalTime.parse(horaInicio)).isBefore(agora)) {
            throw erro("Não é possível agendar em um horário que já passou");
        }
    }

    static String observacoes(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String valor = texto.trim();
        if (valor.length() > 500) {
            throw erro("Observações deve ter no máximo 500 caracteres");
        }
        return valor;
    }

    private static ApiException erro(String mensagem) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
