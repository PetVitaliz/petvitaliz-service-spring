package com.petvitaliz.controller;

import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.dto.SubscriptionRequest;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public Map<String, Object> currentPlan(@AuthenticationPrincipal AuthenticatedUser user) {
        return subscriptionService.findCurrentPlan(user.id())
                .<Map<String, Object>>map(plan -> Map.of("tem_plano", true, "include", plan))
                .orElse(Map.of("mensagem", "Você ainda não possui nenhum plano ativo"));
    }

    @PostMapping("/pagamento")
    public MessageResponse subscribe(@AuthenticationPrincipal AuthenticatedUser user,
                                     @Valid @RequestBody SubscriptionRequest request) {
        subscriptionService.subscribe(user, request.productId());
        return new MessageResponse("Plano assinado com sucesso");
    }

    @DeleteMapping("/cancelar")
    public MessageResponse cancel(@AuthenticationPrincipal AuthenticatedUser user) {
        subscriptionService.cancel(user.id());
        return new MessageResponse("Plano cancelado com sucesso");
    }
}
