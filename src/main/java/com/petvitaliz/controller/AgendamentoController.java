package com.petvitaliz.controller;

import com.petvitaliz.dto.AgendamentoRequest;
import com.petvitaliz.security.UsuarioToken;
import com.petvitaliz.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping("/agendamento")
    public ResponseEntity<Map<String, String>> agendar(@AuthenticationPrincipal UsuarioToken usuario,
                                                       @Valid @RequestBody AgendamentoRequest req) {
        agendamentoService.agendar(usuario.id(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Consulta agendada com sucesso"));
    }

    @GetMapping("/consultas")
    public Map<String, Object> consultas(@AuthenticationPrincipal UsuarioToken usuario) {
        return Map.of("consultas", agendamentoService.listar(usuario.id()));
    }

    @DeleteMapping("/user/agendamento/cancelar/{id}")
    public Map<String, String> cancelar(@AuthenticationPrincipal UsuarioToken usuario,
                                        @PathVariable Integer id) {
        agendamentoService.cancelar(usuario.id(), id);
        return Map.of("mensagem", "Agendamento cancelado com sucesso.");
    }
}
