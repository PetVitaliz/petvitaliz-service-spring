package com.petvitaliz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADM_ID")
    private Integer id;

    @Column(name = "ADM_NOME", nullable = false, unique = true, length = 500)
    private String nome;

    @Column(name = "ADM_EMAIL", nullable = false, unique = true, length = 500)
    private String email;

    @Column(name = "ADM_SENHA", nullable = false, length = 500)
    private String senha;

    @Column(name = "ADM_ATIVO", nullable = false)
    private Boolean ativo = true;

    @Column(name = "ADM_FOTO_URL", length = 1000)
    private String fotoUrl;
}
