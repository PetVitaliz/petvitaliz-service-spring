package com.petvitaliz.entity;

import com.petvitaliz.enums.ConsultaStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ConsultaStatusConverter implements AttributeConverter<ConsultaStatus, String> {

    @Override
    public String convertToDatabaseColumn(ConsultaStatus status) {
        return status == null ? null : status.getValorBanco();
    }

    @Override
    public ConsultaStatus convertToEntityAttribute(String valor) {
        return valor == null ? null : ConsultaStatus.fromBanco(valor);
    }
}
