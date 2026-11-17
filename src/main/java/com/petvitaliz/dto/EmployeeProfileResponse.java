package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.enums.EmployeeSpecialty;

public record EmployeeProfileResponse(
        @JsonProperty("nome") String firstName,
        @JsonProperty("sobrenome") String lastName,
        String email,
        @JsonProperty("foto_url") String photoUrl,
        @JsonProperty("especialidade") EmployeeSpecialty specialty
) {

    public static EmployeeProfileResponse of(Employee employee) {
        return new EmployeeProfileResponse(
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhotoUrl(),
                employee.getSpecialty());
    }
}
