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

    List<Pet> findByOwnerId(Integer ownerId);

    Optional<Pet> findByIdAndOwnerId(Integer id, Integer ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pet p where p.id = :id")
    Optional<Pet> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pet p where p.id = :id and p.owner.id = :ownerId")
    Optional<Pet> findByIdAndOwnerIdForUpdate(@Param("id") Integer id, @Param("ownerId") Integer ownerId);

    @Query("select p from Pet p join fetch p.owner order by p.name asc")
    List<Pet> findAllWithOwner();
}
