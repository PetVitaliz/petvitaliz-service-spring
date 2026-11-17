package com.petvitaliz.security;

import com.petvitaliz.web.JsonResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        TokenType type = TokenType.forPath(request.getRequestURI());
        boolean invalidToken = Boolean.TRUE.equals(
                request.getAttribute(JwtAuthenticationFilter.INVALID_TOKEN_ATTRIBUTE));

        if (invalidToken) {
            JsonResponseWriter.write(response, HttpStatus.FORBIDDEN, type.getExpiredMessage());
        } else {
            JsonResponseWriter.write(response, HttpStatus.UNAUTHORIZED, type.getUnauthenticatedMessage());
        }
    }
}
