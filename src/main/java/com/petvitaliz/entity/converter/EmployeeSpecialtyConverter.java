package com.petvitaliz.entity.converter;

import com.petvitaliz.enums.EmployeeSpecialty;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmployeeSpecialtyConverter extends DbEnumConverter<EmployeeSpecialty> {

    public EmployeeSpecialtyConverter() {
        super(EmployeeSpecialty.class);
    }
}
