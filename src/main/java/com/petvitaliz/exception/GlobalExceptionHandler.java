package com.petvitaliz.exception;

import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.web.JsonText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApi(ApiException ex) {
        if (ex.isPlainBody()) {
            return JsonText.response(ex.getStatus(), ex.getMessage());
        }
        return message(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return message(HttpStatus.BAD_REQUEST, "Id inválido");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        log.error("Unhandled error", ex);
        return message(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String firstError = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Dados inválidos");
        return message(HttpStatus.BAD_REQUEST, firstError);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return message(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido");
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        return message(HttpStatus.PAYLOAD_TOO_LARGE, "A imagem deve ter no máximo 4MB");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode status,
                                                             WebRequest request) {
        return ResponseEntity.status(status).headers(headers).body(new MessageResponse(defaultMessage(status)));
    }

    private static String defaultMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "Rota não encontrada";
            case 405 -> "Método não permitido";
            case 415 -> "Formato da requisição não suportado";
            default -> status.is4xxClientError() ? "Requisição inválida" : "Erro interno do servidor";
        };
    }

    private static ResponseEntity<Object> message(HttpStatus status, String text) {
        return ResponseEntity.status(status).body(new MessageResponse(text));
    }
}
