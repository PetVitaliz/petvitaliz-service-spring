package com.petvitaliz.service;

import com.petvitaliz.entity.Appointment;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.enums.EmployeeSpecialty;
import com.petvitaliz.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentRulesTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 20);

    @Test
    void serviceAcceptsOnlyGroomerAndVeterinarian() {
        assertThat(AppointmentRules.service(" Tosador ")).isEqualTo(EmployeeSpecialty.GROOMER);
        assertThat(AppointmentRules.service("VETERINARIO")).isEqualTo(EmployeeSpecialty.VETERINARIAN);
        assertThatThrownBy(() -> AppointmentRules.service("financeiro")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> AppointmentRules.service(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void startTimeIsNormalizedToTwoDigits() {
        assertThat(AppointmentRules.startTime("9:00")).isEqualTo("09:00");
        assertThat(AppointmentRules.startTime(" 14:30 ")).isEqualTo("14:30");
        assertThat(AppointmentRules.startTime("23:59")).isEqualTo("23:59");
    }

    @Test
    void invalidStartTimeIsRejected() {
        for (String invalid : new String[]{"24:00", "12:60", "abc", "", "12", "12:5"}) {
            assertThatThrownBy(() -> AppointmentRules.startTime(invalid)).isInstanceOf(ApiException.class);
        }
        assertThatThrownBy(() -> AppointmentRules.startTime(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void endTimeAddsOneHourAndWrapsAfterMidnight() {
        assertThat(AppointmentRules.endTime("09:00")).isEqualTo("10:00");
        assertThat(AppointmentRules.endTime("23:30")).isEqualTo("00:30");
    }

    @Test
    void dateAcceptsPlainAndIsoFormats() {
        assertThat(AppointmentRules.date("2026-10-20")).isEqualTo(DAY);
        assertThat(AppointmentRules.date("2026-10-20T00:00:00.000Z")).isEqualTo(DAY);
    }

    @Test
    void invalidDateIsRejected() {
        for (String invalid : new String[]{"2026-02-30", "20/10/2026", "", "amanha"}) {
            assertThatThrownBy(() -> AppointmentRules.date(invalid)).isInstanceOf(ApiException.class);
        }
        assertThatThrownBy(() -> AppointmentRules.date(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void pastSlotIsRejected() {
        LocalDateTime now = DAY.atTime(10, 0);

        assertThatThrownBy(() -> AppointmentRules.requireFuture(DAY, "09:59", now)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> AppointmentRules.requireFuture(DAY.minusDays(1), "23:00", now))
                .isInstanceOf(ApiException.class);
        assertThatCode(() -> AppointmentRules.requireFuture(DAY, "10:00", now)).doesNotThrowAnyException();
        assertThatCode(() -> AppointmentRules.requireFuture(DAY.plusDays(1), "08:00", now)).doesNotThrowAnyException();
    }

    @Test
    void notesHandleBlankAndTooLongText() {
        assertThat(AppointmentRules.notes(null)).isEmpty();
        assertThat(AppointmentRules.notes("   ")).isEmpty();
        assertThat(AppointmentRules.notes(" vacina ")).isEqualTo("vacina");
        assertThatThrownBy(() -> AppointmentRules.notes("a".repeat(501))).isInstanceOf(ApiException.class);
    }

    @Test
    void targetStatusAcceptsTheLegacyTypo() {
        assertThat(AppointmentRules.targetStatus("em_andamento")).isEqualTo(AppointmentStatus.IN_PROGRESS);
        assertThat(AppointmentRules.targetStatus("em_endamento")).isEqualTo(AppointmentStatus.IN_PROGRESS);
        assertThat(AppointmentRules.targetStatus("finalizado")).isEqualTo(AppointmentStatus.FINISHED);
        assertThatThrownBy(() -> AppointmentRules.targetStatus("em_espera")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> AppointmentRules.targetStatus(null)).isInstanceOf(ApiException.class);
    }

    @Test
    void appointmentCanOnlyStartAfterItsStartTime() {
        Appointment appointment = appointment("14:00", "15:00");

        assertThatThrownBy(() -> AppointmentRules.requireTransitionAllowed(
                appointment, AppointmentStatus.IN_PROGRESS, DAY.atTime(13, 59))).isInstanceOf(ApiException.class);
        assertThatCode(() -> AppointmentRules.requireTransitionAllowed(
                appointment, AppointmentStatus.IN_PROGRESS, DAY.atTime(14, 0))).doesNotThrowAnyException();
    }

    @Test
    void appointmentCanOnlyFinishInsideTheToleranceWindow() {
        Appointment appointment = appointment("14:00", "15:00");

        assertThatThrownBy(() -> AppointmentRules.requireTransitionAllowed(
                appointment, AppointmentStatus.FINISHED, DAY.atTime(14, 59))).isInstanceOf(ApiException.class);
        assertThatCode(() -> AppointmentRules.requireTransitionAllowed(
                appointment, AppointmentStatus.FINISHED, DAY.atTime(15, 10))).doesNotThrowAnyException();
        assertThatThrownBy(() -> AppointmentRules.requireTransitionAllowed(
                appointment, AppointmentStatus.FINISHED, DAY.atTime(15, 11))).isInstanceOf(ApiException.class);
    }

    @Test
    void finishedAppointmentCannotChange() {
        Appointment appointment = appointment("14:00", "15:00");
        appointment.setStatus(AppointmentStatus.FINISHED);

        assertThatThrownBy(() -> AppointmentRules.requireTransitionAllowed(
                appointment, AppointmentStatus.IN_PROGRESS, DAY.atTime(14, 30))).isInstanceOf(ApiException.class);
    }

    @Test
    void endAfterMidnightBelongsToTheNextDay() {
        assertThat(AppointmentRules.endOf(appointment("23:30", "00:30"))).isEqualTo(DAY.plusDays(1).atTime(0, 30));
    }

    private static Appointment appointment(String startTime, String endTime) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(DAY);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.WAITING);
        return appointment;
    }
}
