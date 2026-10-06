package com.petvitaliz.controller;

import com.petvitaliz.dto.AlterarSenhaRequest;
import com.petvitaliz.dto.CadastroRequest;
import com.petvitaliz.dto.ConfirmarCodigoRequest;
import com.petvitaliz.dto.LoginRequest;
import com.petvitaliz.dto.LoginResultado;
import com.petvitaliz.dto.PedirResetRequest;
import com.petvitaliz.security.CookieService;
import com.petvitaliz.security.TipoToken;
import com.petvitaliz.security.UsuarioToken;
import com.petvitaliz.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    public AuthController(AuthService authService, CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Map<String, String>> cadastro(@Valid @RequestBody CadastroRequest req) {
        authService.cadastrar(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Usuário cadastrado com sucesso"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return comCookie(TipoToken.USUARIO, authService.loginUsuario(req));
    }

    @PostMapping("/login/adm")
    public ResponseEntity<Map<String, Object>> loginAdm(@Valid @RequestBody LoginRequest req) {
        return comCookie(TipoToken.ADMINISTRADOR, authService.loginAdm(req));
    }

    @PostMapping("/login/funcionario")
    public ResponseEntity<Map<String, Object>> loginFuncionario(@Valid @RequestBody LoginRequest req) {
        return comCookie(TipoToken.FUNCIONARIO, authService.loginFuncionario(req));
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie apagar = cookieService.limpar(TipoToken.USUARIO.getNomeCookie());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.comoCabecalho(apagar, false))
                .body(Map.of("message", "Logout realizado com sucesso"));
    }

    @PostMapping("/login/esqueci-a-senha")
    public ResponseEntity<Map<String, String>> esqueciASenha(@Valid @RequestBody PedirResetRequest req) {
        authService.pedirResetSenha(req.email());
        return ResponseEntity.ok(Map.of("mensagem", "Codigo de reset de senha enviado para o email"));
    }

    @PostMapping("/login/esqueci-a-senha-confirmar")
    public ResponseEntity<Map<String, Object>> confirmarCodigo(@Valid @RequestBody ConfirmarCodigoRequest req) {
        return comCookie(TipoToken.RESET, authService.confirmarCodigo(req));
    }

    @PutMapping("/login/alterar-senha")
    public ResponseEntity<Map<String, String>> alterarSenha(@AuthenticationPrincipal UsuarioToken usuario,
                                                            @Valid @RequestBody AlterarSenhaRequest req) {
        authService.alterarSenha(usuario.id(), req);
        ResponseCookie apagar = cookieService.limpar(TipoToken.RESET.getNomeCookie());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.comoCabecalho(apagar, true))
                .body(Map.of("mensagem", "Senha alterado com sucesso"));
    }

    private ResponseEntity<Map<String, Object>> comCookie(TipoToken tipo, LoginResultado resultado) {
        ResponseCookie cookie = cookieService.criar(tipo.getNomeCookie(), resultado.token(), tipo.getValidade());
        boolean particionado = tipo == TipoToken.RESET;
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.comoCabecalho(cookie, particionado))
                .body(resultado.corpo());
    }
}
