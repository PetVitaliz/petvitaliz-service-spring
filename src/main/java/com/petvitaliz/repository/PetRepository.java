package com.petvitaliz.repository;

import com.petvitaliz.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Integer> {

    List<Pet> findByUsuarioId(Integer idUsuario);
}
