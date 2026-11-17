package com.petvitaliz.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final boolean plainBody;

    public ApiException(HttpStatus status, String message) {
        this(status, message, false);
    }

    private ApiException(HttpStatus status, String message, boolean plainBody) {
        super(message);
        this.status = status;
        this.plainBody = plainBody;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException plain(HttpStatus status, String message) {
        return new ApiException(status, message, true);
    }
}
