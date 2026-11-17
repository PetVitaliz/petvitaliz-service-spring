package com.petvitaliz.entity;

import com.petvitaliz.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "consulta")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @Column(name = "id_pet", insertable = false, updatable = false)
    private Integer petId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Employee employee;

    @Column(name = "id_funcionario", insertable = false, updatable = false)
    private Integer employeeId;

    @Column(name = "data_consulta", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "hora_inicio", nullable = false, length = 10)
    private String startTime;

    @Column(name = "hora_fim", nullable = false, length = 10)
    private String endTime;

    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Column(name = "observacoes", nullable = false, length = 500)
    private String notes = "";
}
