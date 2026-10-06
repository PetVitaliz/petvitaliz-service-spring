package com.petvitaliz.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_TOKEN_INVALIDO = "petvitaliz.tokenInvalido";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        TipoToken tipo = TipoToken.paraCaminho(request.getRequestURI());
        String token = lerCookie(request, tipo.getNomeCookie());

        if (token != null && !token.isBlank()) {
            try {
                Claims claims = jwtService.validar(token);

                if (!tipo.getRole().equals(claims.get("role", String.class))) {
                    throw new JwtException("Tipo de token incorreto");
                }
                if (tipo == TipoToken.RESET && !Boolean.TRUE.equals(claims.get("reset_autorizado", Boolean.class))) {
                    throw new JwtException("Reset nao autorizado");
                }

                UsuarioToken principal = new UsuarioToken(
                        claims.get("id", Integer.class),
                        claims.get("email", String.class),
                        claims.get("nome", String.class),
                        claims.get("sobrenome", String.class),
                        tipo.getRole());

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + tipo.getRole()))));
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(ATRIBUTO_TOKEN_INVALIDO, true);
            }
        }

        chain.doFilter(request, response);
    }

    private String lerCookie(HttpServletRequest request, String nome) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (nome.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
