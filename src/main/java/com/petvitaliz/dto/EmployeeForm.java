package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.BindParam;

public record EmployeeForm(
        @JsonProperty("nome") @BindParam("nome") String firstName,
        @JsonProperty("sobrenome") @BindParam("sobrenome") String lastName,
        String email,
        @JsonProperty("especialidade") @BindParam("especialidade") String specialty,
        @JsonProperty("ativo") @BindParam("ativo") String active,
        @JsonProperty("senha") @BindParam("senha") String password
) {
}
