package com.petvitaliz.repository;

import com.petvitaliz.entity.Appointment;
import com.petvitaliz.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Query("select a from Appointment a join fetch a.pet p join fetch a.employee "
            + "where p.owner.id = :ownerId order by a.appointmentDate desc, a.startTime asc")
    List<Appointment> findAllByOwner(@Param("ownerId") Integer ownerId);

    @Query("select a from Appointment a join fetch a.employee "
            + "where a.id = :id and a.pet.owner.id = :ownerId")
    Optional<Appointment> findByIdAndOwner(@Param("id") Integer id, @Param("ownerId") Integer ownerId);

    @Query("select a from Appointment a join fetch a.pet p join fetch p.owner "
            + "where a.employee.id = :employeeId order by a.appointmentDate desc, a.startTime asc")
    List<Appointment> findAllByEmployee(@Param("employeeId") Integer employeeId);

    @Query("select a from Appointment a join fetch a.pet p join fetch p.owner "
            + "where a.employee.id = :employeeId and a.appointmentDate = :date order by a.startTime asc")
    List<Appointment> findByEmployeeAndDate(@Param("employeeId") Integer employeeId, @Param("date") LocalDate date);

    @Query("select a from Appointment a join fetch a.pet p join fetch p.owner "
            + "where a.id = :id and a.employee.id = :employeeId")
    Optional<Appointment> findByIdAndEmployee(@Param("id") Integer id, @Param("employeeId") Integer employeeId);

    @Query("select a from Appointment a join fetch a.pet p join fetch p.owner join fetch a.employee "
            + "where a.appointmentDate = :date order by a.startTime asc")
    List<Appointment> findAllByDate(@Param("date") LocalDate date);

    boolean existsByPetIdAndAppointmentDateAndStartTimeAndStatusNot(
            Integer petId, LocalDate appointmentDate, String startTime, AppointmentStatus status);

    @Query("select a.employee.id, a.startTime, a.endTime from Appointment a "
            + "where a.appointmentDate = :date and a.employee.id in :employeeIds")
    List<Object[]> findSchedulesOfDay(@Param("date") LocalDate date,
                                      @Param("employeeIds") Collection<Integer> employeeIds);

    @Query("select distinct a.employee.id from Appointment a "
            + "where a.appointmentDate = :date and a.employee.id in :employeeIds "
            + "and a.startTime < :endTime and a.endTime > :startTime")
    List<Integer> findBusyEmployeeIds(@Param("date") LocalDate date,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime,
                                      @Param("employeeIds") Collection<Integer> employeeIds);
}
