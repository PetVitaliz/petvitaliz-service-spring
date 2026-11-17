package com.petvitaliz.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum AppointmentStatus implements DbEnum {
    WAITING("em espera", "em_espera"),
    IN_PROGRESS("em andamento", "em_andamento"),
    FINISHED("finalizado", "finalizado");

    private final String dbValue;

    @Getter(onMethod_ = @JsonValue)
    private final String apiValue;

    public static Optional<AppointmentStatus> fromApiValue(String apiValue) {
        for (AppointmentStatus status : values()) {
            if (status.apiValue.equals(apiValue)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
