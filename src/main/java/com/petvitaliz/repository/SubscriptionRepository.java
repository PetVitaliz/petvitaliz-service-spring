package com.petvitaliz.repository;

import com.petvitaliz.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    Optional<Subscription> findByPetId(Integer petId);

    Optional<Subscription> findByPetIdAndUserId(Integer petId, Integer userId);

    @Query("select s from Subscription s join fetch s.product where s.userId = :userId order by s.id")
    List<Subscription> findAllByUserIdWithProduct(@Param("userId") Integer userId);

    @Query("select s from Subscription s join fetch s.product")
    List<Subscription> findAllWithProduct();

    @Modifying
    @Query("delete from Subscription s where s.petId = :petId")
    void deleteByPetId(@Param("petId") Integer petId);

    @Modifying
    @Query("delete from Subscription")
    int deleteAllSubscriptions();
}
