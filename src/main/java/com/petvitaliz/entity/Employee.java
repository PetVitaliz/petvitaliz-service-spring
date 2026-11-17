package com.petvitaliz.entity;

import com.petvitaliz.enums.EmployeeSpecialty;
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
@Table(name = "funcionario")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    public static final int MAX_WORKLOAD = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionario")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 500)
    private String firstName;

    @Column(name = "sobrenome", nullable = false, length = 500)
    private String lastName;

    @Column(name = "especialidade", nullable = false)
    private EmployeeSpecialty specialty;

    @Column(nullable = false, unique = true, length = 500)
    private String email;

    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @Column(name = "carga", nullable = false)
    private Integer workload = MAX_WORKLOAD;

    @Column(name = "senha", nullable = false, length = 500)
    private String password;

    @Column(name = "foto_url", length = 1000)
    private String photoUrl;
}
