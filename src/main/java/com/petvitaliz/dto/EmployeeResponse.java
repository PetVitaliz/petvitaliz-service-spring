package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.enums.EmployeeSpecialty;

public record EmployeeResponse(
        @JsonProperty("id_funcionario") Integer id,
        @JsonProperty("nome") String firstName,
        @JsonProperty("sobrenome") String lastName,
        String email,
        @JsonProperty("especialidade") EmployeeSpecialty specialty,
        @JsonProperty("ativo") Boolean active,
        @JsonProperty("foto_url") String photoUrl,
        @JsonProperty("carga") Integer workload
) {

    public static EmployeeResponse of(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getSpecialty(),
                employee.getActive(),
                employee.getPhotoUrl(),
                employee.getWorkload());
    }
}
