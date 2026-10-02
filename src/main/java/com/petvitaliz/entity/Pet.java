package com.petvitaliz.entity;

import com.petvitaliz.enums.PetEspecie;
import com.petvitaliz.enums.PetSexo;
import jakarta.persistence.*;
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
    private Usuario usuario;

    @Column(nullable = false, length = 500)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetEspecie especie;

    @Column(name = "outra_especie", length = 100)
    private String outraEspecie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetSexo sexo;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "foto_url", length = 1000)
    private String fotoUrl;

    @Column(nullable = false)
    private Integer idade;

    private Double peso;

    @Column(length = 1000)
    private String observacoes;
}
