package com.petvitaliz.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PetSex {
    M("Macho"),
    F("Fêmea");

    private final String displayName;
}
