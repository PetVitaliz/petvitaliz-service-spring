package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.enums.EmployeeSpecialty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeSummaryResponse(
        @JsonProperty("nome") String firstName,
        @JsonProperty("sobrenome") String lastName,
        @JsonProperty("especialidade") EmployeeSpecialty specialty,
        @JsonProperty("carga") Integer workload
) {

    public static EmployeeSummaryResponse fullNameOf(Employee employee) {
        return new EmployeeSummaryResponse(employee.getFirstName(), employee.getLastName(), null, null);
    }

    public static EmployeeSummaryResponse nameAndSpecialtyOf(Employee employee) {
        return new EmployeeSummaryResponse(employee.getFirstName(), null, employee.getSpecialty(), null);
    }

    public static EmployeeSummaryResponse workloadOf(Employee employee) {
        return new EmployeeSummaryResponse(
                employee.getFirstName(), employee.getLastName(), employee.getSpecialty(), employee.getWorkload());
    }
}
