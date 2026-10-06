package com.petvitaliz.dto;

import java.util.Map;

/** O token (vai no cookie) e o corpo JSON da resposta. */
public record LoginResultado(String token, Map<String, Object> corpo) {
}
