package com.petvitaliz.controller;

import com.petvitaliz.dto.LoginRequest;
import com.petvitaliz.dto.LoginResult;
import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.dto.NewPasswordRequest;
import com.petvitaliz.dto.PasswordResetRequest;
import com.petvitaliz.dto.ResetCodeRequest;
import com.petvitaliz.dto.SignupRequest;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.security.CookieService;
import com.petvitaliz.security.TokenType;
import com.petvitaliz.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthController {

    public static final String CLIENT_TYPE_HEADER = "X-Client-Type";

    private static final Set<String> TOKEN_CLIENTS = Set.of("mobile", "web");

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/cadastro")
    public ResponseEntity<MessageResponse> signUp(@Valid @RequestBody SignupRequest request) {
        authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Usuário cadastrado com sucesso"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(name = CLIENT_TYPE_HEADER, required = false) String clientType) {
        return withCookie(TokenType.USER, authService.loginUser(request), clientType);
    }

    @PostMapping("/login/adm")
    public ResponseEntity<Map<String, Object>> loginAdmin(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(name = CLIENT_TYPE_HEADER, required = false) String clientType) {
        return withCookie(TokenType.ADMIN, authService.loginAdmin(request), clientType);
    }

    @PostMapping("/login/funcionario")
    public ResponseEntity<Map<String, Object>> loginEmployee(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(name = CLIENT_TYPE_HEADER, required = false) String clientType) {
        return withCookie(TokenType.EMPLOYEE, authService.loginEmployee(request), clientType);
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> logout() {
        return LogoutResponses.clearing(cookieService, TokenType.USER);
    }

    @PostMapping("/login/esqueci-a-senha")
    public MessageResponse requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return new MessageResponse("Codigo de reset de senha enviado para o email");
    }

    @PostMapping("/login/esqueci-a-senha-confirmar")
    public ResponseEntity<Map<String, Object>> confirmResetCode(@Valid @RequestBody ResetCodeRequest request) {
        return withCookie(TokenType.PASSWORD_RESET, authService.confirmResetCode(request), null);
    }

    @PutMapping("/login/alterar-senha")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal AuthenticatedUser user,
                                                          @Valid @RequestBody NewPasswordRequest request) {
        authService.changePassword(user.id(), request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.clear(TokenType.PASSWORD_RESET))
                .body(new MessageResponse("Senha alterado com sucesso"));
    }

    private ResponseEntity<Map<String, Object>> withCookie(TokenType type, LoginResult result, String clientType) {
        Map<String, Object> body = new LinkedHashMap<>(result.body());
        if (clientType != null && TOKEN_CLIENTS.contains(clientType.trim().toLowerCase())) {
            body.put("token", result.token());
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.issue(type, result.token()))
                .body(body);
    }
}
