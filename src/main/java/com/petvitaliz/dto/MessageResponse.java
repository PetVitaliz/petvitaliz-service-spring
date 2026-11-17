package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageResponse(@JsonProperty("mensagem") String message) {
}
