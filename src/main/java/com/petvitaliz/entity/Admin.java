package com.petvitaliz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADM_ID")
    private Integer id;

    @Column(name = "ADM_NOME", nullable = false, unique = true, length = 500)
    private String username;

    @Column(name = "ADM_EMAIL", nullable = false, unique = true, length = 500)
    private String email;

    @Column(name = "ADM_SENHA", nullable = false, length = 500)
    private String password;

    @Column(name = "ADM_ATIVO", nullable = false)
    private Boolean active = true;

    @Column(name = "ADM_FOTO_URL", length = 1000)
    private String photoUrl;
}
