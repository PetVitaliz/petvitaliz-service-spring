package com.petvitaliz.web;

import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class JsonText {

    private JsonText() {
    }

    public static String quote(String text) {
        return TextNode.valueOf(text).toString();
    }

    public static ResponseEntity<String> response(HttpStatus status, String text) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(quote(text));
    }
}
