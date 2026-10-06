package com.petvitaliz.entity;

import com.petvitaliz.enums.FuncionarioEspecialidade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "funcionario")
@Getter
@Setter
@NoArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionario")
    private Integer id;

    @Column(nullable = false, length = 500)
    private String nome;

    @Column(nullable = false, length = 500)
    private String sobrenome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuncionarioEspecialidade especialidade;

    @Column(nullable = false, unique = true, length = 500)
    private String email;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    private Integer carga = 8;

    @Column(nullable = false, length = 500)
    private String senha;

    @Column(name = "foto_url", length = 1000)
    private String fotoUrl;
}
