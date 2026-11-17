package com.petvitaliz.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PetSpecies implements DbEnum {
    DOG("cachorro", "Cão"),
    CAT("gato", "Gato"),
    BIRD("ave", "Ave"),
    RABBIT("coelho", "Coelho"),
    OTHER("outro", "Outro");

    @Getter(onMethod_ = @JsonValue)
    private final String dbValue;

    private final String displayName;
}
