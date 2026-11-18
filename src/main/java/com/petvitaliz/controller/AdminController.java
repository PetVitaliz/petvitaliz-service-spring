package com.petvitaliz.controller;

import com.petvitaliz.dto.AdminClientResponse;
import com.petvitaliz.dto.AppointmentRequest;
import com.petvitaliz.dto.AppointmentResponse;
import com.petvitaliz.dto.AppointmentStatusRequest;
import com.petvitaliz.dto.AvailabilityResponse;
import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.security.CookieService;
import com.petvitaliz.security.TokenType;
import com.petvitaliz.service.AdminOverviewService;
import com.petvitaliz.service.AppointmentService;
import com.petvitaliz.service.CloudinaryService;
import com.petvitaliz.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm")
@RequiredArgsConstructor
public class AdminController {

    private final AdminOverviewService overviewService;
    private final AppointmentService appointmentService;
    private final PetService petService;
    private final CloudinaryService cloudinaryService;
    private final CookieService cookieService;

    @GetMapping
    public Map<String, String> home() {
        return Map.of("pagina", "Home Administrador");
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> logout() {
        return LogoutResponses.clearing(cookieService, TokenType.ADMIN);
    }

    @GetMapping("/consultas")
    public Map<String, List<AppointmentResponse>> todayAppointments() {
        return Map.of("consultas", overviewService.listTodayAppointments());
    }

    @PutMapping("/consultas/atualizar/{id}")
    public MessageResponse updateAppointmentStatus(@PathVariable Integer id,
                                                   @RequestBody AppointmentStatusRequest request) {
        appointmentService.updateStatusAsAdmin(id, request);
        return new MessageResponse("Status atualizado com sucesso");
    }

    @DeleteMapping("/consultas/cancelar/{id}")
    public MessageResponse cancelAppointment(@PathVariable Integer id) {
        appointmentService.cancelAsAdmin(id);
        return new MessageResponse("Agendamento cancelado com sucesso.");
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

    @DeleteMapping("/listar/clientes/excluir/{petId}")
    public MessageResponse deleteClientPet(@PathVariable Integer petId) {
        petService.deleteAsAdmin(petId);
        return new MessageResponse("Pet deletado com sucesso");
    }

    @GetMapping("/listar/clientes")
    public Map<String, List<AdminClientResponse>> clients() {
        return Map.of("clientes", overviewService.listClients());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam(name = "image", required = false) MultipartFile image) {
        if (!CloudinaryService.hasFile(image)) {
            throw ApiException.badRequest("Nenhum arquivo enviado");
        }
        return Map.of(
                "mensagem", "Upload realizado com sucesso",
                "url", cloudinaryService.upload(image, CloudinaryService.DEFAULT_FOLDER));
    }
}
