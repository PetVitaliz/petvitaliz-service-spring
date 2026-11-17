package com.petvitaliz.dto;

import java.util.Map;

public record LoginResult(String token, Map<String, Object> body) {
}
