package com.petvitaliz.controller;

import com.petvitaliz.dto.AppointmentRequest;
import com.petvitaliz.dto.AppointmentResponse;
import com.petvitaliz.dto.AvailabilityResponse;
import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/agendamento")
    public ResponseEntity<MessageResponse> schedule(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @Valid @RequestBody AppointmentRequest request) {
        appointmentService.schedule(user.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Consulta agendada com sucesso"));
    }

    @GetMapping("/agendamento/horarios")
    public AvailabilityResponse availability(@RequestParam(name = "servico", required = false) String service,
                                             @RequestParam(name = "data", required = false) String date) {
        return appointmentService.availability(service, date);
    }

    @GetMapping("/consultas")
    public Map<String, List<AppointmentResponse>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of("consultas", appointmentService.list(user.id()));
    }

    @DeleteMapping("/user/agendamento/cancelar/{id}")
    public MessageResponse cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Integer id) {
        appointmentService.cancel(user.id(), id);
        return new MessageResponse("Agendamento cancelado com sucesso.");
    }
}
