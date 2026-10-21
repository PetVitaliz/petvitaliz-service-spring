package com.petvitaliz.service;

public record PetForm(
        String nome,
        String especie,
        String outraEspecie,
        String sexo,
        String dataNascimento,
        String idade,
        String peso,
        String observacoes
) {
}
