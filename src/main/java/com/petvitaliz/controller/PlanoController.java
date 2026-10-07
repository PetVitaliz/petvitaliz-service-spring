package com.petvitaliz.controller;

import com.petvitaliz.dto.PagamentoRequest;
import com.petvitaliz.security.UsuarioToken;
import com.petvitaliz.service.PlanoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/planos")
public class PlanoController {

    private final PlanoService planoService;

    public PlanoController(PlanoService planoService) {
        this.planoService = planoService;
    }

    @GetMapping
    public Map<String, Object> planoAtual(@AuthenticationPrincipal UsuarioToken usuario) {
        return planoService.buscarPlanoAtual(usuario.id())
                .<Map<String, Object>>map(plano -> Map.of("tem_plano", true, "include", plano))
                .orElse(Map.of("mensagem", "Você ainda não possui nenhum plano ativo"));
    }

    @PostMapping("/pagamento")
    public Map<String, String> pagamento(@AuthenticationPrincipal UsuarioToken usuario,
                                         @Valid @RequestBody PagamentoRequest req) {
        planoService.assinar(usuario, req.idProduto());
        return Map.of("mensagem", "Plano assinado com sucesso");
    }

    @DeleteMapping("/cancelar")
    public Map<String, String> cancelar(@AuthenticationPrincipal UsuarioToken usuario) {
        planoService.cancelar(usuario.id());
        return Map.of("mensagem", "Plano cancelado com sucesso");
    }
}
