package com.petvitaliz.service;

import com.petvitaliz.dto.AppointmentRequest;
import com.petvitaliz.dto.AppointmentResponse;
import com.petvitaliz.dto.AppointmentStatusRequest;
import com.petvitaliz.entity.Appointment;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.enums.EmployeeSpecialty;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.AppointmentRepository;
import com.petvitaliz.repository.EmployeeRepository;
import com.petvitaliz.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final String NO_PROFESSIONAL =
            "Nenhum profissional disponivel no momento, desculpe a inconveniencia";
    private static final String SLOT_TAKEN = "Este horrario ja possui uma consulta ativa";

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PetRepository petRepository;
    private final Clock clock;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void schedule(Integer ownerId, AppointmentRequest request) {
        book(request, () -> petRepository.findByIdAndOwnerIdForUpdate(request.petId(), ownerId));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void scheduleAsAdmin(AppointmentRequest request) {
        book(request, () -> petRepository.findByIdForUpdate(request.petId()));
    }

    private void book(AppointmentRequest request, Supplier<Optional<Pet>> petLookup) {
        EmployeeSpecialty specialty = AppointmentRules.service(request.service());
        LocalDate date = AppointmentRules.date(request.date());
        String startTime = AppointmentRules.startTime(request.startTime());
        AppointmentRules.requireFuture(date, startTime, LocalDateTime.now(clock));
        String notes = AppointmentRules.notes(request.notes());

        Pet pet = petLookup.get().orElseThrow(() -> ApiException.notFound("Pet não encontrado"));

        if (appointmentRepository.existsByPetIdAndAppointmentDateAndStartTimeAndStatusNot(
                pet.getId(), date, startTime, AppointmentStatus.FINISHED)) {
            throw ApiException.conflict("Seu pet já possui um agendamento neste horário.");
        }

        Employee employee = reserveEmployee(specialty, date, startTime);

        Appointment appointment = new Appointment();
        appointment.setPet(pet);
        appointment.setEmployee(employee);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(AppointmentRules.endTime(startTime));
        appointment.setStatus(AppointmentStatus.WAITING);
        appointment.setNotes(notes);
        appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> list(Integer ownerId) {
        List<AppointmentResponse> appointments = appointmentRepository.findAllByOwner(ownerId).stream()
                .map(AppointmentResponse::forOwner)
                .toList();
        if (appointments.isEmpty()) {
            throw ApiException.notFound("Você não possui nenhuma consulta");
        }
        return appointments;
    }

    @Transactional
    public void cancel(Integer ownerId, Integer appointmentId) {
        remove(appointmentRepository.findByIdAndOwner(appointmentId, ownerId));
    }

    @Transactional
    public void cancelAsAdmin(Integer appointmentId) {
        remove(appointmentRepository.findById(appointmentId));
    }

    @Transactional
    public void updateStatusAsAdmin(Integer appointmentId, AppointmentStatusRequest request) {
        AppointmentStatus target = AppointmentRules.adminTargetStatus(request.newStatus());
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> ApiException.notFound("Consulta não encontrada"));
        AppointmentRules.requireNotFinished(appointment);
        appointment.setStatus(target);
    }

    private void remove(Optional<Appointment> found) {
        Appointment appointment = found.orElseThrow(() -> ApiException.notFound("Agendamento não encontrado."));

        if (appointment.getStatus() == AppointmentStatus.FINISHED) {
            throw ApiException.conflict("Consultas finalizadas não podem ser canceladas");
        }

        appointmentRepository.delete(appointment);
        employeeRepository.releaseWorkload(appointment.getEmployeeId(), Employee.MAX_WORKLOAD);
    }

    private Employee reserveEmployee(EmployeeSpecialty specialty, LocalDate date, String startTime) {
        List<Integer> candidateIds = employeeRepository
                .findBySpecialtyAndActiveTrueAndWorkloadGreaterThan(specialty, 0).stream()
                .map(Employee::getId)
                .toList();
        if (candidateIds.isEmpty()) {
            throw ApiException.notFound(NO_PROFESSIONAL);
        }

        Set<Integer> busyIds = new HashSet<>(
                appointmentRepository.findBusyEmployeeIds(date, startTime, candidateIds));
        List<Integer> freeIds = candidateIds.stream().filter(id -> !busyIds.contains(id)).toList();
        if (freeIds.isEmpty()) {
            throw ApiException.conflict(SLOT_TAKEN);
        }

        Integer chosenId = freeIds.get(ThreadLocalRandom.current().nextInt(freeIds.size()));
        Employee employee = employeeRepository.findByIdForUpdate(chosenId)
                .orElseThrow(() -> ApiException.notFound(NO_PROFESSIONAL));

        if (appointmentRepository.existsByEmployeeIdAndAppointmentDateAndStartTime(chosenId, date, startTime)) {
            throw ApiException.conflict(SLOT_TAKEN);
        }
        if (employeeRepository.reserveWorkload(chosenId) == 0) {
            throw ApiException.notFound(NO_PROFESSIONAL);
        }
        return employee;
    }
}
