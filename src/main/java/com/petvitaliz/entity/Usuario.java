package com.petvitaliz.entity;

import com.petvitaliz.enums.UsuarioGenero;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(nullable = false, length = 500)
    private String nome;

    @Column(nullable = false, length = 500)
    private String sobrenome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioGenero genero;

    @Column(nullable = false, unique = true, length = 500)
    private String email;

    @Column(nullable = false, length = 500)
    private String senha;

    @Column(nullable = false, unique = true, length = 500)
    private String telefone;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "CPF", nullable = false, unique = true, length = 11)
    private String cpf;
}
