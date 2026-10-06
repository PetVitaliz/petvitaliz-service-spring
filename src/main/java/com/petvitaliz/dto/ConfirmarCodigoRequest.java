package com.petvitaliz.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ATENCAO: diferente da API Express, agora o e-mail tambem e enviado.
 * Sem ele, o codigo de 6 digitos de QUALQUER usuario liberava a troca de senha.
 */
public record ConfirmarCodigoRequest(

        @NotBlank(message = "Email é obrigatório")
        String email,

        @NotBlank(message = "Codigo invalido")
        String codigo
) {
}
