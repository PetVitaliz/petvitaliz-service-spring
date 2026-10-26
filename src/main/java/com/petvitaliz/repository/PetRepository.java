package com.petvitaliz.repository;

import com.petvitaliz.entity.Pet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Integer> {

    List<Pet> findByUsuarioId(Integer idUsuario);

    Optional<Pet> findByIdAndUsuarioId(Integer id, Integer idUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pet p where p.id = :id and p.usuario.id = :idUsuario")
    Optional<Pet> buscarDoUsuarioComTrava(@Param("id") Integer id, @Param("idUsuario") Integer idUsuario);
}
