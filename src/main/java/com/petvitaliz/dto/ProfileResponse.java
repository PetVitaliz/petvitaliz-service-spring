package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.User;
import com.petvitaliz.util.DateParser;

public record ProfileResponse(
        @JsonProperty("nome") String firstName,
        @JsonProperty("sobrenome") String lastName,
        String email,
        @JsonProperty("CPF") String cpf,
        @JsonProperty("telefone") String phone,
        @JsonProperty("data_nascimento") String birthDate
) {

    public static ProfileResponse of(User user) {
        return new ProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCpf(),
                user.getPhone(),
                DateParser.toIsoMidnight(user.getBirthDate()));
    }
}
