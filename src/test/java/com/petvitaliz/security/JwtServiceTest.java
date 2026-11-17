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

    private static final String SECRET = "a-test-key-with-more-than-32-characters";
    private static final String OTHER_SECRET = "another-test-key-also-longer-than-32-chars";

    @Test
    void generatesAndParsesToken() {
        JwtService service = new JwtService(SECRET);

        Claims claims = service.parse(service.generate(TokenType.ADMIN, Map.of(JwtClaims.ID, 7)));

        assertThat(claims.get(JwtClaims.ID, Integer.class)).isEqualTo(7);
        assertThat(claims.get(JwtClaims.ROLE, String.class)).isEqualTo("ADMIN");
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = new JwtService(SECRET).generate(Map.of(JwtClaims.ID, 1), Duration.ofMinutes(5));

        assertThatThrownBy(() -> new JwtService(OTHER_SECRET).parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService service = new JwtService(SECRET);
        String token = service.generate(Map.of(JwtClaims.ID, 1), Duration.ofSeconds(-10));

        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void refusesShortSecret() {
        assertThatThrownBy(() -> new JwtService("short")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void picksTheCookieByRequestPath() {
        assertThat(TokenType.forPath("/adm/listar/funcionario")).isEqualTo(TokenType.ADMIN);
        assertThat(TokenType.forPath("/adm")).isEqualTo(TokenType.ADMIN);
        assertThat(TokenType.forPath("/funcionario/consultas")).isEqualTo(TokenType.EMPLOYEE);
        assertThat(TokenType.forPath("/user/login/alterar-senha")).isEqualTo(TokenType.PASSWORD_RESET);
        assertThat(TokenType.forPath("/user/listar/pet")).isEqualTo(TokenType.USER);
        assertThat(TokenType.forPath("/administracao")).isEqualTo(TokenType.USER);
    }
}
