package com.petvitaliz.repository;

import com.petvitaliz.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Integer> {

    List<Pet> findByUsuarioId(Integer idUsuario);

    Optional<Pet> findByIdAndUsuarioId(Integer id, Integer idUsuario);
}
