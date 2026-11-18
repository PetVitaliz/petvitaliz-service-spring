package com.petvitaliz.controller;

import com.petvitaliz.dto.AppointmentRequest;
import com.petvitaliz.dto.AppointmentResponse;
import com.petvitaliz.dto.AppointmentStatusRequest;
import com.petvitaliz.dto.AvailabilityResponse;
import com.petvitaliz.dto.ClientResponse;
import com.petvitaliz.dto.EmployeeHomeResponse;
import com.petvitaliz.dto.EmployeeProfileRequest;
import com.petvitaliz.dto.EmployeeProfileResponse;
import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.dto.PatientResponse;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.security.CookieService;
import com.petvitaliz.security.TokenType;
import com.petvitaliz.service.AppointmentService;
import com.petvitaliz.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/funcionario")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AppointmentService appointmentService;
    private final CookieService cookieService;

    @GetMapping
    public EmployeeHomeResponse home(@AuthenticationPrincipal AuthenticatedUser employee) {
        return employeeService.home(employee.id());
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> logout() {
        return LogoutResponses.clearing(cookieService, TokenType.EMPLOYEE);
    }

    @GetMapping("/consultas")
    public Map<String, List<AppointmentResponse>> appointments(@AuthenticationPrincipal AuthenticatedUser employee) {
        return Map.of("consultas", employeeService.listAppointments(employee.id()));
    }

    @GetMapping("/consultas/{id}")
    public Map<String, AppointmentResponse> appointment(@AuthenticationPrincipal AuthenticatedUser employee,
                                                        @PathVariable Integer id) {
        return Map.of("consulta", employeeService.getAppointment(employee.id(), id));
    }

    @PutMapping("/consultas/atualizar/{id}")
    public MessageResponse updateAppointmentStatus(@AuthenticationPrincipal AuthenticatedUser employee,
                                                   @PathVariable Integer id,
                                                   @RequestBody AppointmentStatusRequest request) {
        employeeService.updateAppointmentStatus(employee.id(), id, request);
        return new MessageResponse("Status atualizado com sucesso");
    }

    @PostMapping("/bater-ponto")
    public MessageResponse clockIn(@AuthenticationPrincipal AuthenticatedUser employee) {
        employeeService.clockIn(employee.id());
        return new MessageResponse("Ponto batido com sucesso (Carga restaurada para 8).");
    }

    @GetMapping("/perfil-dados")
    public EmployeeProfileResponse profile(@AuthenticationPrincipal AuthenticatedUser employee) {
        return employeeService.getProfile(employee.id());
    }

    @PutMapping("/perfil-dados/atualizar")
    public Map<String, Object> updateProfile(@AuthenticationPrincipal AuthenticatedUser employee,
                                             @RequestBody EmployeeProfileRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Perfil atualizado com sucesso");
        body.put("funcionario", employeeService.updateProfile(employee.id(), request));
        return body;
    }

    @GetMapping("/agendamento/horarios")
    public AvailabilityResponse availability(@RequestParam(name = "servico", required = false) String service,
                                             @RequestParam(name = "data", required = false) String date) {
        return appointmentService.availability(service, date);
    }

    @PostMapping("/agendamento")
    public ResponseEntity<MessageResponse> schedule(@Valid @RequestBody AppointmentRequest request) {
        appointmentService.scheduleAsAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Consulta agendada com sucesso"));
    }

    @GetMapping("/clientes")
    public Map<String, List<ClientResponse>> clients() {
        return Map.of("clientes", employeeService.listClients());
    }

    @GetMapping("/pets")
    public Map<String, List<PatientResponse>> patients() {
        return Map.of("pets", employeeService.listPatients());
    }
}
