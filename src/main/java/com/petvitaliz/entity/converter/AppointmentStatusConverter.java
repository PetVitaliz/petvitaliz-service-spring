package com.petvitaliz.entity.converter;

import com.petvitaliz.enums.AppointmentStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AppointmentStatusConverter extends DbEnumConverter<AppointmentStatus> {

    private static final String LEGACY_IN_PROGRESS = "em endamento";

    public AppointmentStatusConverter() {
        super(AppointmentStatus.class);
    }

    @Override
    public AppointmentStatus convertToEntityAttribute(String dbValue) {
        if (LEGACY_IN_PROGRESS.equals(dbValue)) {
            return AppointmentStatus.IN_PROGRESS;
        }
        return super.convertToEntityAttribute(dbValue);
    }
}
