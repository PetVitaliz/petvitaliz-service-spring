package com.petvitaliz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppointmentStatusRequest(@JsonProperty("novoStatus") String newStatus) {
}
