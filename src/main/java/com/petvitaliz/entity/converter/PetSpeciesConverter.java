package com.petvitaliz.entity.converter;

import com.petvitaliz.enums.PetSpecies;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PetSpeciesConverter extends DbEnumConverter<PetSpecies> {

    public PetSpeciesConverter() {
        super(PetSpecies.class);
    }
}
