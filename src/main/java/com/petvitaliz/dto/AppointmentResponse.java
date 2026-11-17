package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Appointment;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.util.DateParser;

public record AppointmentResponse(
        @JsonProperty("id_consulta") Integer id,
        @JsonProperty("id_pet") Integer petId,
        @JsonProperty("id_funcionario") Integer employeeId,
        @JsonProperty("data_consulta") String date,
        @JsonProperty("hora_inicio") String startTime,
        @JsonProperty("hora_fim") String endTime,
        AppointmentStatus status,
        @JsonProperty("observacoes") String notes,
        @JsonInclude(JsonInclude.Include.NON_NULL) Object pet,
        @JsonProperty("funcionario") @JsonInclude(JsonInclude.Include.NON_NULL) EmployeeSummaryResponse employee
) {

    public static AppointmentResponse forOwner(Appointment appointment) {
        return of(appointment,
                PetSummaryResponse.nameOf(appointment.getPet()),
                EmployeeSummaryResponse.nameAndSpecialtyOf(appointment.getEmployee()));
    }

    public static AppointmentResponse forEmployeeAgenda(Appointment appointment) {
        Pet pet = appointment.getPet();
        return of(appointment, PetResponse.withOwner(pet, OwnerResponse.nameOf(pet.getOwner())), null);
    }

    public static AppointmentResponse forEmployeeList(Appointment appointment) {
        return of(appointment, PetSummaryResponse.of(appointment.getPet()), null);
    }

    public static AppointmentResponse forEmployeeDetails(Appointment appointment) {
        Pet pet = appointment.getPet();
        return of(appointment, PetResponse.withOwner(pet, OwnerResponse.contactOf(pet.getOwner())), null);
    }

    public static AppointmentResponse forAdmin(Appointment appointment) {
        Pet pet = appointment.getPet();
        return of(appointment,
                PetResponse.withOwner(pet, OwnerResponse.nameOf(pet.getOwner())),
                EmployeeSummaryResponse.fullNameOf(appointment.getEmployee()));
    }

    private static AppointmentResponse of(Appointment appointment, Object pet, EmployeeSummaryResponse employee) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPetId(),
                appointment.getEmployeeId(),
                DateParser.toIsoMidnight(appointment.getAppointmentDate()),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getNotes(),
                pet,
                employee);
    }
}
