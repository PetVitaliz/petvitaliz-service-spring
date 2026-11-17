package com.petvitaliz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petvitaliz.dto.MessageResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonResponseWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonResponseWriter() {
    }

    public static void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(MAPPER.writeValueAsString(new MessageResponse(message)));
    }
}
