package com.petvitaliz.repository;

import com.petvitaliz.entity.Funcionario;
import com.petvitaliz.enums.FuncionarioEspecialidade;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    Optional<Funcionario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    List<Funcionario> findByEspecialidadeAndAtivoTrueAndCargaGreaterThan(
            FuncionarioEspecialidade especialidade, Integer carga);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Funcionario f where f.id = :id")
    Optional<Funcionario> buscarComTrava(@Param("id") Integer id);

    @Modifying(flushAutomatically = true)
    @Query("update Funcionario f set f.carga = f.carga - 1 "
            + "where f.id = :id and f.carga > 0 and f.ativo = true")
    int reservarCarga(@Param("id") Integer id);

    @Modifying(flushAutomatically = true)
    @Query("update Funcionario f set f.carga = case when f.carga < :maximo then f.carga + 1 else f.carga end "
            + "where f.id = :id")
    int devolverCarga(@Param("id") Integer id, @Param("maximo") Integer maximo);
}
