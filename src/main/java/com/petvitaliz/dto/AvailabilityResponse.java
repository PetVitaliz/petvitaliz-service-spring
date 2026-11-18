package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AvailabilityResponse(

        @JsonProperty("data")
        String date,

        @JsonProperty("servico")
        String service,

        @JsonProperty("profissionais")
        int professionals,

        @JsonProperty("indisponiveis")
        List<String> unavailableTimes
) {
}
