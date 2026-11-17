package com.petvitaliz.repository;

import com.petvitaliz.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    Optional<Subscription> findFirstByUserId(Integer userId);
}
