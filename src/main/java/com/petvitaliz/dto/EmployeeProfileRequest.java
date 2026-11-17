package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmployeeProfileRequest(
        @JsonProperty("nome") String firstName,
        @JsonProperty("sobrenome") String lastName,
        String email
) {
}
