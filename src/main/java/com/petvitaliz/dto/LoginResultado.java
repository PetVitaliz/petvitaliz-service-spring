package com.petvitaliz.dto;

import java.util.Map;

public record LoginResultado(String token, Map<String, Object> corpo) {
}
