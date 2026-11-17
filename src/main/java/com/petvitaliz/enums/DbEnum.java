package com.petvitaliz.enums;

import java.util.Optional;

public interface DbEnum {

    String getDbValue();

    static <E extends Enum<E> & DbEnum> Optional<E> find(Class<E> type, String dbValue) {
        if (dbValue == null) {
            return Optional.empty();
        }
        String value = dbValue.trim().toLowerCase();
        for (E constant : type.getEnumConstants()) {
            if (constant.getDbValue().equals(value)) {
                return Optional.of(constant);
            }
        }
        return Optional.empty();
    }
}
