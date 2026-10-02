package com.petvitaliz.repository;

import com.petvitaliz.entity.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Integer> {

    Optional<Assinatura> findFirstByIdUsuario(Integer idUsuario);
}
