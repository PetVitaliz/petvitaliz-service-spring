package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petvitaliz.entity.Admin;

public record AdminResponse(
        @JsonProperty("ADM_ID") Integer id,
        @JsonProperty("ADM_NOME") String username,
        @JsonProperty("ADM_EMAIL") String email,
        @JsonProperty("ADM_ATIVO") Boolean active,
        @JsonProperty("ADM_FOTO_URL") String photoUrl
) {

    public static AdminResponse of(Admin admin) {
        return new AdminResponse(
                admin.getId(), admin.getUsername(), admin.getEmail(), admin.getActive(), admin.getPhotoUrl());
    }
}
