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

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "beneficios", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
