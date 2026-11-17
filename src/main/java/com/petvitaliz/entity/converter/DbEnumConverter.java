package com.petvitaliz.entity.converter;

import com.petvitaliz.enums.DbEnum;
import jakarta.persistence.AttributeConverter;

public abstract class DbEnumConverter<E extends Enum<E> & DbEnum> implements AttributeConverter<E, String> {

    private final Class<E> type;

    protected DbEnumConverter(Class<E> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public E convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return DbEnum.find(type, dbValue).orElseThrow(() ->
                new IllegalArgumentException("Unknown " + type.getSimpleName() + " value: " + dbValue));
    }
}
