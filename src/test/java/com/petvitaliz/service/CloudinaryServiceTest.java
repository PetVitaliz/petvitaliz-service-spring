package com.petvitaliz.service;

import com.petvitaliz.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudinaryServiceTest {

    private final CloudinaryService service = new CloudinaryService("", "", "");

    @Test
    void nomeSeguroRemoveExtensaoECaracteresPerigosos() {
        assertThat(CloudinaryService.nomeSeguro("meu pet (1).jpg")).isEqualTo("meupet1");
        assertThat(CloudinaryService.nomeSeguro("../../etc/passwd.png")).isEqualTo("pet");
        assertThat(CloudinaryService.nomeSeguro(null)).isEqualTo("pet");
        assertThat(CloudinaryService.nomeSeguro("***.png")).isEqualTo("pet");
        assertThat(CloudinaryService.nomeSeguro("a".repeat(80) + ".png")).hasSize(50);
    }

    @Test
    void aceitaJpegEPngPequenos() {
        MockMultipartFile jpg = new MockMultipartFile("image", "a.jpg", "image/jpeg", new byte[10]);
        MockMultipartFile png = new MockMultipartFile("image", "a.png", "image/png", new byte[10]);
        assertThatCode(() -> service.validar(jpg)).doesNotThrowAnyException();
        assertThatCode(() -> service.validar(png)).doesNotThrowAnyException();
    }

    @Test
    void rejeitaTipoInvalido() {
        MockMultipartFile pdf = new MockMultipartFile("image", "a.pdf", "application/pdf", new byte[10]);
        assertThatThrownBy(() -> service.validar(pdf)).isInstanceOf(ApiException.class);
    }

    @Test
    void rejeitaArquivoMaiorQue4MB() {
        MockMultipartFile grande = new MockMultipartFile("image", "a.jpg", "image/jpeg",
                new byte[(int) CloudinaryService.TAMANHO_MAXIMO + 1]);
        assertThatThrownBy(() -> service.validar(grande)).isInstanceOf(ApiException.class);
    }
}
