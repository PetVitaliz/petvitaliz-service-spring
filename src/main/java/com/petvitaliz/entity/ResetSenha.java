package com.petvitaliz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reset_senha")
@Getter
@Setter
@NoArgsConstructor
public class ResetSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Codigo de 6 digitos enviado por e-mail. */
    @Column(nullable = false)
    private String token;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;
}
