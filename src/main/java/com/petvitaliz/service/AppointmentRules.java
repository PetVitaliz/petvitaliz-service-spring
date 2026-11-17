package com.petvitaliz.service;

import com.petvitaliz.entity.Appointment;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.enums.DbEnum;
import com.petvitaliz.enums.EmployeeSpecialty;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.util.DateParser;
import com.petvitaliz.util.TimeParser;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

final class AppointmentRules {

    static final Duration FINISH_TOLERANCE = Duration.ofMinutes(10);

    private static final Duration LENGTH = Duration.ofHours(1);
    private static final int MAX_NOTES_LENGTH = 500;
    private static final Set<EmployeeSpecialty> BOOKABLE =
            Set.of(EmployeeSpecialty.GROOMER, EmployeeSpecialty.VETERINARIAN);
    private static final String LEGACY_IN_PROGRESS = "em_endamento";

    private AppointmentRules() {
    }

    static EmployeeSpecialty service(String service) {
        return DbEnum.find(EmployeeSpecialty.class, service)
                .filter(BOOKABLE::contains)
                .orElseThrow(() -> ApiException.badRequest("no momento so temos 'tosador' e 'veterinario'"));
    }

    static LocalDate date(String text) {
        return DateParser.parse(text).orElseThrow(() ->
                ApiException.badRequest("Data da consulta inválida (use AAAA-MM-DD)"));
    }

    static String startTime(String text) {
        return TimeParser.parse(text)
                .map(TimeParser::format)
                .orElseThrow(() -> ApiException.badRequest("Hora de início inválida (use HH:mm)"));
    }

    static String endTime(String startTime) {
        return TimeParser.format(LocalTime.parse(startTime).plus(LENGTH));
    }

    static void requireFuture(LocalDate date, String startTime, LocalDateTime now) {
        if (LocalDateTime.of(date, LocalTime.parse(startTime)).isBefore(now)) {
            throw ApiException.badRequest("Não é possível agendar em um horário que já passou");
        }
    }

    static String notes(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String value = text.trim();
        if (value.length() > MAX_NOTES_LENGTH) {
            throw ApiException.badRequest("Observações deve ter no máximo 500 caracteres");
        }
        return value;
    }

    static AppointmentStatus targetStatus(String text) {
        return parseStatus(text)
                .filter(status -> status != AppointmentStatus.WAITING)
                .orElseThrow(() -> ApiException.badRequest("Status inválido. Use 'em_andamento' ou 'finalizado'"));
    }

    static AppointmentStatus adminTargetStatus(String text) {
        return parseStatus(text).orElseThrow(() ->
                ApiException.badRequest("Status inválido. Use 'em_espera', 'em_andamento' ou 'finalizado'"));
    }

    static void requireNotFinished(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.FINISHED) {
            throw ApiException.badRequest("Esta consulta já foi finalizada");
        }
    }

    private static Optional<AppointmentStatus> parseStatus(String text) {
        String value = text == null ? "" : text.trim();
        if (LEGACY_IN_PROGRESS.equals(value)) {
            return Optional.of(AppointmentStatus.IN_PROGRESS);
        }
        return AppointmentStatus.fromApiValue(value);
    }

    static LocalDateTime startOf(Appointment appointment) {
        LocalTime time = TimeParser.parse(appointment.getStartTime()).orElse(LocalTime.MIN);
        return LocalDateTime.of(appointment.getAppointmentDate(), time);
    }

    static LocalDateTime endOf(Appointment appointment) {
        LocalDateTime start = startOf(appointment);
        LocalTime time = TimeParser.parse(appointment.getEndTime()).orElse(start.toLocalTime().plus(LENGTH));
        LocalDateTime end = LocalDateTime.of(appointment.getAppointmentDate(), time);
        return end.isAfter(start) ? end : end.plusDays(1);
    }

    static void requireTransitionAllowed(Appointment appointment, AppointmentStatus target, LocalDateTime now) {
        requireNotFinished(appointment);
        if (target == AppointmentStatus.IN_PROGRESS && now.isBefore(startOf(appointment))) {
            throw ApiException.badRequest("Não é possível iniciar este atendimento antes do horário agendado ("
                    + appointment.getStartTime() + ").");
        }
        if (target == AppointmentStatus.FINISHED) {
            LocalDateTime end = endOf(appointment);
            if (now.isBefore(end)) {
                throw ApiException.badRequest("Você só pode encerrar este atendimento a partir de "
                        + appointment.getEndTime() + ".");
            }
            if (now.isAfter(end.plus(FINISH_TOLERANCE))) {
                throw ApiException.badRequest(
                        "Janela de finalização expirada. Reporte ao Administrador do sistema.");
            }
        }
    }
}
