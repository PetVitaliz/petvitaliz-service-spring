package com.petvitaliz.controller;

import com.petvitaliz.dto.ContactRequest;
import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.dto.ProductResponse;
import com.petvitaliz.dto.ProfileResponse;
import com.petvitaliz.dto.ProfileUpdateRequest;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.service.ProductService;
import com.petvitaliz.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private static final String LOGIN_NOTICE = "faça login para utilizar esse recurso";

    private final ProductService productService;
    private final UserService userService;

    @GetMapping("/")
    public Map<String, Object> publicHome() {
        List<ProductResponse> products = productService.listForCatalog();

        Map<String, Object> body = publicPage("Home", null);
        if (products.isEmpty()) {
            body.put("aviso", "faça login para utilizar outros recursos.");
            body.put("produtos", "nenhum produto cadastrado no momento");
        } else {
            body.put("aviso", "faça login para utilizar outros recursos");
            body.put("produtos", products);
        }
        return body;
    }

    @GetMapping("/servicos")
    public Map<String, Object> publicServices() {
        return publicPage("Pagina de serviços", null);
    }

    @GetMapping("/contato")
    public Map<String, Object> publicContact() {
        return publicPage("Pagina de contato", LOGIN_NOTICE);
    }

    @GetMapping("/servicos-de-emergencia")
    public Map<String, Object> publicEmergencyServices() {
        return publicPage("Pagina de serviços de emergencia", null);
    }

    @GetMapping("/listar/pet")
    public Map<String, Object> publicPetList() {
        return publicPage("Pagina de listar pet", LOGIN_NOTICE);
    }

    @GetMapping("/cadastar/pet")
    public Map<String, Object> publicPetCreate() {
        return publicPage("Pagina de cadastrar pet", LOGIN_NOTICE);
    }

    @GetMapping("/editar/pet/{id}")
    public Map<String, Object> publicPetEdit() {
        return publicPage("Pagina de editar pet", LOGIN_NOTICE);
    }

    @GetMapping("/delete/pet/{id}")
    public Map<String, Object> publicPetDelete() {
        return publicPage("Pagina de excluir pet", LOGIN_NOTICE);
    }

    @GetMapping("/user/home")
    public Map<String, Object> home(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of("pagina", "Home Logada", "usuario", user.fullName());
    }

    @GetMapping("/user/servicos")
    public Map<String, Object> services(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of("pagina", "Serviços Logado", "usuario", user.fullName());
    }

    @GetMapping("/user/servicos-de-emergencia")
    public Map<String, Object> emergencyServices(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of("mensagem", "Pagina de serviços de emergencia", "usuario", user.fullName());
    }

    @PostMapping("/user/contato")
    public MessageResponse contact(@AuthenticationPrincipal AuthenticatedUser user,
                                   @Valid @RequestBody ContactRequest request) {
        userService.sendContact(user, request);
        return new MessageResponse("Email enviado com sucesso");
    }

    @GetMapping("/user/buscar/perfil")
    public ProfileResponse profile(@AuthenticationPrincipal AuthenticatedUser user) {
        return userService.getProfile(user.id());
    }

    @PutMapping("/user/perfil-dados/atualizar")
    public Map<String, Object> updateProfile(@AuthenticationPrincipal AuthenticatedUser user,
                                             @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse profile = userService.updateProfile(user.id(), request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Dados atualizados com sucesso");
        body.put("perfil", profile);
        return body;
    }

    private static Map<String, Object> publicPage(String page, String notice) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pagina", page);
        body.put("versão", "não logada");
        if (notice != null) {
            body.put("aviso", notice);
        }
        return body;
    }
}
