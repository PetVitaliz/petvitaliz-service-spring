package com.petvitaliz.repository;

import com.petvitaliz.entity.ResetSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetSenhaRepository extends JpaRepository<ResetSenha, Integer> {

    Optional<ResetSenha> findFirstByUsuarioIdOrderByExpiraEmDesc(Integer idUsuario);

    void deleteByUsuarioId(Integer idUsuario);
}
