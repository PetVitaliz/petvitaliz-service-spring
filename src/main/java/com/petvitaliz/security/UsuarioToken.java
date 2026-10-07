package com.petvitaliz.security;

public record UsuarioToken(Integer id, String email, String nome, String sobrenome, String role) {
}
