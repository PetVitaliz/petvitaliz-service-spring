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
    void safeNameDropsExtensionAndDangerousCharacters() {
        assertThat(CloudinaryService.safeName("meu pet (1).jpg")).isEqualTo("meupet1");
        assertThat(CloudinaryService.safeName("../../etc/passwd.png")).isEqualTo("pet");
        assertThat(CloudinaryService.safeName(null)).isEqualTo("pet");
        assertThat(CloudinaryService.safeName("***.png")).isEqualTo("pet");
        assertThat(CloudinaryService.safeName("a".repeat(80) + ".png")).hasSize(50);
    }

    @Test
    void acceptsSmallJpegAndPngFiles() {
        MockMultipartFile jpg = new MockMultipartFile("image", "a.jpg", "image/jpeg", new byte[10]);
        MockMultipartFile png = new MockMultipartFile("image", "a.png", "image/png", new byte[10]);

        assertThatCode(() -> service.validate(jpg)).doesNotThrowAnyException();
        assertThatCode(() -> service.validate(png)).doesNotThrowAnyException();
        assertThatCode(() -> service.validate(null)).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnsupportedContentType() {
        MockMultipartFile pdf = new MockMultipartFile("image", "a.pdf", "application/pdf", new byte[10]);

        assertThatThrownBy(() -> service.validate(pdf)).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsFilesLargerThanFourMegabytes() {
        MockMultipartFile large = new MockMultipartFile("image", "a.jpg", "image/jpeg",
                new byte[(int) CloudinaryService.MAX_FILE_SIZE + 1]);

        assertThatThrownBy(() -> service.validate(large)).isInstanceOf(ApiException.class);
    }

    @Test
    void keepsTheCurrentUrlWhenNoFileIsSent() {
        assertThat(service.uploadOrKeep(null, CloudinaryService.DEFAULT_FOLDER, "https://cdn/current.png"))
                .isEqualTo("https://cdn/current.png");
    }
}
