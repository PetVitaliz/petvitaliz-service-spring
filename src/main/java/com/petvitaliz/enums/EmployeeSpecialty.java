package com.petvitaliz.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeSpecialty implements DbEnum {
    VETERINARIAN("veterinario"),
    GROOMER("tosador"),
    RECEPTIONIST("recepcionista"),
    FINANCIAL("financeiro");

    @Getter(onMethod_ = @JsonValue)
    private final String dbValue;
}
