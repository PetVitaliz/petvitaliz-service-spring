package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Appointment;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.enums.EmployeeSpecialty;
import com.petvitaliz.util.DateParser;

public record MonthAppointmentResponse(
        @JsonProperty("id_consulta") Integer id,
        @JsonProperty("id_pet") Integer petId,
        @JsonProperty("data_consulta") String date,
        @JsonProperty("hora_inicio") String startTime,
        AppointmentStatus status,
        @JsonProperty("observacoes") String notes,
        @JsonProperty("servico") EmployeeSpecialty service
) {

    public static MonthAppointmentResponse of(Appointment appointment) {
        return new MonthAppointmentResponse(
                appointment.getId(),
                appointment.getPetId(),
                DateParser.toIsoMidnight(appointment.getAppointmentDate()),
                appointment.getStartTime(),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getEmployee().getSpecialty());
    }
}
