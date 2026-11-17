package com.petvitaliz.repository;

import com.petvitaliz.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer> {

    Optional<PasswordReset> findFirstByUserIdOrderByExpiresAtDesc(Integer userId);

    void deleteByUserId(Integer userId);
}
