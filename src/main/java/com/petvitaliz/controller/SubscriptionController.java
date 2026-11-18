package com.petvitaliz.controller;

import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.dto.PetPlanResponse;
import com.petvitaliz.dto.SubscriptionRequest;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/planos")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public Map<String, Object> plans(@AuthenticationPrincipal AuthenticatedUser user,
                                     @RequestParam(name = "id_pet", required = false) Integer petId) {
        List<PetPlanResponse> plans = subscriptionService.listPlans(user.id()).stream()
                .filter(plan -> petId == null || plan.petId().equals(petId))
                .toList();
        if (plans.isEmpty()) {
            return Map.of("mensagem", petId == null
                    ? "Você ainda não possui nenhum plano ativo"
                    : "Este pet ainda não possui plano ativo", "planos", List.of());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tem_plano", true);
        body.put("include", plans.get(0).product());
        body.put("planos", plans);
        return body;
    }

    @PostMapping("/pagamento")
    public MessageResponse subscribe(@AuthenticationPrincipal AuthenticatedUser user,
                                     @Valid @RequestBody SubscriptionRequest request) {
        subscriptionService.subscribe(user, request.productId(), request.petId());
        return new MessageResponse("Plano assinado com sucesso");
    }

    @DeleteMapping("/cancelar")
    public MessageResponse cancel(@AuthenticationPrincipal AuthenticatedUser user,
                                  @RequestParam(name = "id_pet", required = false) Integer petId) {
        subscriptionService.cancel(user.id(), petId);
        return new MessageResponse("Plano cancelado com sucesso");
    }

    @DeleteMapping("/cancelar/{petId}")
    public MessageResponse cancelOfPet(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Integer petId) {
        subscriptionService.cancel(user.id(), petId);
        return new MessageResponse("Plano cancelado com sucesso");
    }
}
