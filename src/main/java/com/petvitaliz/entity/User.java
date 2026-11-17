package com.petvitaliz.entity;

import com.petvitaliz.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 500)
    private String firstName;

    @Column(name = "sobrenome", nullable = false, length = 500)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false)
    private Gender gender;

    @Column(nullable = false, unique = true, length = 500)
    private String email;

    @Column(name = "senha", nullable = false, length = 500)
    private String password;

    @Column(name = "telefone", nullable = false, unique = true, length = 500)
    private String phone;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate birthDate;

    @Column(name = "CPF", nullable = false, unique = true, length = 11)
    private String cpf;

    @OneToMany(mappedBy = "owner")
    private List<Pet> pets = new ArrayList<>();
}
