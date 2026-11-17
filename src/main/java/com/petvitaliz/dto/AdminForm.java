package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.BindParam;

public record AdminForm(
        String username,
        String email,
        @JsonProperty("senha") @BindParam("senha") String password,
        @JsonProperty("ativo") @BindParam("ativo") String active
) {
}
