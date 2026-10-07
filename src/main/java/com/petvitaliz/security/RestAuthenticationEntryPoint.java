package com.petvitaliz.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 401 quando nao tem cookie; 403 quando o cookie existe mas o token e invalido/expirado.
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        TipoToken tipo = TipoToken.paraCaminho(request.getRequestURI());
        boolean tokenInvalido = Boolean.TRUE.equals(
                request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_TOKEN_INVALIDO));

        if (tokenInvalido) {
            RespostaJson.escrever(response, HttpServletResponse.SC_FORBIDDEN, tipo.getMensagemSessaoExpirada());
        } else {
            RespostaJson.escrever(response, HttpServletResponse.SC_UNAUTHORIZED, tipo.getMensagemNaoAutenticado());
        }
    }
}
