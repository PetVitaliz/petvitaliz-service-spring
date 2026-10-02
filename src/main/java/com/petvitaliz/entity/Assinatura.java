package com.petvitaliz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "assinaturas")
@Getter
@Setter
@NoArgsConstructor
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_assinatura")
    private Integer id;

    /** No banco original nao existe FK para usuario, entao guardamos so o id. */
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(name = "data_assinatura")
    private LocalDateTime dataAssinatura;

    @PrePersist
    void definirDataAssinatura() {
        if (dataAssinatura == null) {
            dataAssinatura = LocalDateTime.now();
        }
    }
}
