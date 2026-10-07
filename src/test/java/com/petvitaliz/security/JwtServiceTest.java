package com.petvitaliz.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SEGREDO = "uma-chave-de-teste-com-mais-de-32-caracteres";
    private static final String OUTRO_SEGREDO = "outra-chave-de-teste-tambem-com-mais-de-32-chars";

    @Test
    void geraEValidaToken() {
        JwtService service = new JwtService(SEGREDO);

        String token = service.gerar(Map.of("id", 7, "role", "USUARIO"), Duration.ofMinutes(5));
        Claims claims = service.validar(token);

        assertThat(claims.get("id", Integer.class)).isEqualTo(7);
        assertThat(claims.get("role", String.class)).isEqualTo("USUARIO");
    }

    @Test
    void rejeitaTokenAssinadoComOutraChave() {
        String token = new JwtService(SEGREDO).gerar(Map.of("id", 1), Duration.ofMinutes(5));

        assertThatThrownBy(() -> new JwtService(OUTRO_SEGREDO).validar(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaTokenExpirado() {
        JwtService service = new JwtService(SEGREDO);
        String token = service.gerar(Map.of("id", 1), Duration.ofSeconds(-10));

        assertThatThrownBy(() -> service.validar(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void recusaSegredoCurto() {
        assertThatThrownBy(() -> new JwtService("curta"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escolheOCookieCertoPeloCaminho() {
        assertThat(TipoToken.paraCaminho("/adm/listar/funcionario")).isEqualTo(TipoToken.ADMINISTRADOR);
        assertThat(TipoToken.paraCaminho("/funcionario/consultas")).isEqualTo(TipoToken.FUNCIONARIO);
        assertThat(TipoToken.paraCaminho("/user/login/alterar-senha")).isEqualTo(TipoToken.RESET);
        assertThat(TipoToken.paraCaminho("/user/listar/pet")).isEqualTo(TipoToken.USUARIO);
        assertThat(TipoToken.paraCaminho("/agendamento")).isEqualTo(TipoToken.USUARIO);
    }
}
