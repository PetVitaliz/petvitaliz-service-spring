package com.petvitaliz.repository;

import com.petvitaliz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByPhone(String phone);

    @Query("select distinct u from User u left join fetch u.pets order by u.firstName asc")
    List<User> findAllWithPets();
}
