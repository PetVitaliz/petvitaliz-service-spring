package com.petvitaliz.service;

import com.petvitaliz.dto.AppointmentResponse;
import com.petvitaliz.dto.AppointmentStatusRequest;
import com.petvitaliz.dto.ClientResponse;
import com.petvitaliz.dto.EmployeeHomeResponse;
import com.petvitaliz.dto.EmployeeProfileRequest;
import com.petvitaliz.dto.EmployeeProfileResponse;
import com.petvitaliz.dto.EmployeeResponse;
import com.petvitaliz.dto.EmployeeSummaryResponse;
import com.petvitaliz.dto.MonthAppointmentResponse;
import com.petvitaliz.dto.PatientResponse;
import com.petvitaliz.entity.Appointment;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.enums.AppointmentStatus;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.AppointmentRepository;
import com.petvitaliz.repository.EmployeeRepository;
import com.petvitaliz.repository.PetRepository;
import com.petvitaliz.repository.UserRepository;
import com.petvitaliz.util.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final SubscriptionService subscriptionService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public EmployeeHomeResponse home(Integer employeeId) {
        Employee employee = find(employeeId);
        List<AppointmentResponse> today = appointmentRepository
                .findByEmployeeAndDate(employeeId, LocalDate.now(clock)).stream()
                .map(AppointmentResponse::forEmployeeAgenda)
                .toList();
        return new EmployeeHomeResponse(EmployeeSummaryResponse.workloadOf(employee), today);
    }

    @Transactional
    public void clockIn(Integer employeeId) {
        find(employeeId).setWorkload(Employee.MAX_WORKLOAD);
    }

    @Transactional
    public List<AppointmentResponse> listAppointments(Integer employeeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Appointment> appointments = appointmentRepository.findAllByEmployee(employeeId);

        appointments.stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.WAITING)
                .filter(appointment -> appointment.getAppointmentDate().equals(now.toLocalDate()))
                .filter(appointment -> !now.isBefore(AppointmentRules.startOf(appointment)))
                .forEach(appointment -> appointment.setStatus(AppointmentStatus.IN_PROGRESS));

        return appointments.stream().map(AppointmentResponse::forEmployeeList).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Integer employeeId, Integer appointmentId) {
        return AppointmentResponse.forEmployeeDetails(findAppointment(employeeId, appointmentId));
    }

    @Transactional
    public void updateAppointmentStatus(Integer employeeId, Integer appointmentId, AppointmentStatusRequest request) {
        AppointmentStatus target = AppointmentRules.targetStatus(request.newStatus());
        Appointment appointment = findAppointment(employeeId, appointmentId);
        AppointmentRules.requireTransitionAllowed(appointment, target, LocalDateTime.now(clock));
        appointment.setStatus(target);
    }

    @Transactional(readOnly = true)
    public EmployeeProfileResponse getProfile(Integer employeeId) {
        return EmployeeProfileResponse.of(find(employeeId));
    }

    @Transactional
    public EmployeeResponse updateProfile(Integer employeeId, EmployeeProfileRequest request) {
        String firstName = Texts.trimToEmpty(request.firstName());
        String email = Texts.normalizeEmail(request.email());
        if (firstName.isEmpty() || email.isEmpty()) {
            throw ApiException.badRequest("Nome e Email são campos obrigatórios.");
        }
        EmployeeRules.requireCorporateEmail(email, ApiException.badRequest(EmployeeRules.CORPORATE_EMAIL_REQUIRED));
        if (employeeRepository.existsByEmailAndIdNot(email, employeeId)) {
            throw ApiException.badRequest(EmployeeRules.EMAIL_IN_USE);
        }

        Employee employee = find(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(Texts.trimToEmpty(request.lastName()));
        employee.setEmail(email);
        return EmployeeResponse.of(employee);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listClients() {
        Map<Integer, String> plans = subscriptionService.planNamesByPet();
        return userRepository.findAllWithPets().stream()
                .map(user -> ClientResponse.of(user, plans))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> listPatients() {
        LocalDate today = LocalDate.now(clock);
        Map<Integer, String> plans = subscriptionService.planNamesByPet();
        Map<Integer, List<MonthAppointmentResponse>> monthAppointments = appointmentRepository
                .findAllBetween(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth())).stream()
                .map(MonthAppointmentResponse::of)
                .collect(Collectors.groupingBy(MonthAppointmentResponse::petId));
        return petRepository.findAllWithOwner().stream()
                .map(pet -> PatientResponse.of(pet, today, plans.get(pet.getId()),
                        monthAppointments.getOrDefault(pet.getId(), List.of())))
                .toList();
    }


    private Employee find(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> ApiException.notFound("Funcionário não encontrado"));
    }

    private Appointment findAppointment(Integer employeeId, Integer appointmentId) {
        PersistenceGuard.requirePositiveId(appointmentId, ApiException.badRequest("Id inválido"));
        return appointmentRepository.findByIdAndEmployee(appointmentId, employeeId)
                .orElseThrow(() -> ApiException.notFound("Consulta não encontrada"));
    }
}
