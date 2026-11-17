package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OwnerResponse(
        @JsonProperty("nome") String firstName,
        @JsonProperty("sobrenome") String lastName,
        String email,
        @JsonProperty("telefone") String phone
) {

    public static OwnerResponse nameOf(User owner) {
        return new OwnerResponse(owner.getFirstName(), owner.getLastName(), null, null);
    }

    public static OwnerResponse contactOf(User owner) {
        return new OwnerResponse(owner.getFirstName(), owner.getLastName(), owner.getEmail(), owner.getPhone());
    }
}
