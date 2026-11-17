package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EmployeeHomeResponse(
        @JsonProperty("funcionario") EmployeeSummaryResponse employee,
        @JsonProperty("consultas") List<AppointmentResponse> appointments
) {
}
