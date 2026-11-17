package com.petvitaliz.repository;

import com.petvitaliz.entity.Employee;
import com.petvitaliz.enums.EmployeeSpecialty;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    List<Employee> findAllByOrderByIdDesc();

    List<Employee> findBySpecialtyAndActiveTrueAndWorkloadGreaterThan(EmployeeSpecialty specialty, Integer workload);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Employee e where e.id = :id")
    Optional<Employee> findByIdForUpdate(@Param("id") Integer id);

    @Modifying(flushAutomatically = true)
    @Query("update Employee e set e.workload = e.workload - 1 "
            + "where e.id = :id and e.workload > 0 and e.active = true")
    int reserveWorkload(@Param("id") Integer id);

    @Modifying(flushAutomatically = true)
    @Query("update Employee e set e.workload = case when e.workload < :max then e.workload + 1 else e.workload end "
            + "where e.id = :id")
    int releaseWorkload(@Param("id") Integer id, @Param("max") Integer max);
}
