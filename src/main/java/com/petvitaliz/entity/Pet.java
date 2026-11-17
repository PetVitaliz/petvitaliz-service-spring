package com.petvitaliz.entity;

import com.petvitaliz.enums.PetSex;
import com.petvitaliz.enums.PetSpecies;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "pet")
@Getter
@Setter
@NoArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pet")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User owner;

    @Column(name = "id_usuario", insertable = false, updatable = false)
    private Integer ownerId;

    @Column(name = "nome", nullable = false, length = 500)
    private String name;

    @Column(name = "especie", nullable = false)
    private PetSpecies species;

    @Column(name = "outra_especie", length = 100)
    private String otherSpecies;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private PetSex sex;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate birthDate;

    @Column(name = "foto_url", length = 1000)
    private String photoUrl;

    @Column(name = "idade", nullable = false)
    private Integer age;

    @Column(name = "peso")
    private Double weight;

    @Column(name = "observacoes", length = 1000)
    private String notes;
}
