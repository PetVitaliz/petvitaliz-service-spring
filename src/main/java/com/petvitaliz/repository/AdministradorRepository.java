package com.petvitaliz.repository;

import com.petvitaliz.entity.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

    Optional<Administrador> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNome(String nome);

    boolean existsByEmailAndIdNot(String email, Integer id);

    boolean existsByNomeAndIdNot(String nome, Integer id);
}
