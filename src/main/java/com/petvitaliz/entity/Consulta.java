package com.petvitaliz.entity;

import com.petvitaliz.enums.ConsultaStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "consulta")
@Getter
@Setter
@NoArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario funcionario;

    @Column(name = "data_consulta", nullable = false)
    private LocalDate dataConsulta;

    /** Formato "HH:mm" (VARCHAR no banco). */
    @Column(name = "hora_inicio", nullable = false, length = 10)
    private String horaInicio;

    @Column(name = "hora_fim", nullable = false, length = 10)
    private String horaFim;

    @Convert(converter = ConsultaStatusConverter.class)
    @Column(nullable = false)
    private ConsultaStatus status;

    @Column(nullable = false, length = 500)
    private String observacoes = "";
}
