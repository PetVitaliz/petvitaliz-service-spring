package com.petvitaliz.repository;

import com.petvitaliz.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    Optional<Subscription> findFirstByUserId(Integer userId);

    @Modifying
    @Query("delete from Subscription")
    int deleteAllSubscriptions();
}
