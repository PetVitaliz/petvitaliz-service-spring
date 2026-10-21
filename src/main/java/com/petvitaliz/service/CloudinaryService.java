package com.petvitaliz.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.petvitaliz.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    static final long TAMANHO_MAXIMO = 4L * 1024 * 1024;
    private static final Set<String> TIPOS_ACEITOS = Set.of("image/jpeg", "image/png", "image/jpg");

    private final Cloudinary cloudinary;
    private final boolean configurado;

    public CloudinaryService(@Value("${app.cloudinary.cloud-name:}") String cloudName,
                             @Value("${app.cloudinary.api-key:}") String apiKey,
                             @Value("${app.cloudinary.api-secret:}") String apiSecret) {
        this.configurado = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public void validar(MultipartFile arquivo) {
        if (!TIPOS_ACEITOS.contains(String.valueOf(arquivo.getContentType()).toLowerCase(Locale.ROOT))) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Tipo de arquivo inválido. Apenas JPEG, PNG e JPG são aceitos");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A imagem deve ter no máximo 4MB");
        }
    }

    public String enviarImagem(MultipartFile arquivo) {
        validar(arquivo);

        if (!configurado) {
            log.error("Cloudinary nao configurado: defina CLOUDINARY_NAME, CLOUDINARY_API_KEY e CLOUDINARY_API_SECRET");
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
        }

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(arquivo.getBytes(), ObjectUtils.asMap(
                    "folder", "petvitaliz",
                    "public_id", System.currentTimeMillis() + "-" + nomeSeguro(arquivo.getOriginalFilename()),
                    "resource_type", "image"));

            Object url = resultado.get("secure_url");
            if (url == null) {
                throw new IllegalStateException("Resposta do Cloudinary sem secure_url");
            }
            return url.toString();
        } catch (IOException | RuntimeException e) {
            log.error("Erro ao enviar imagem para o Cloudinary", e);
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar a imagem agora. Tente novamente em instantes");
        }
    }

    static String nomeSeguro(String nomeOriginal) {
        String base = nomeOriginal == null ? "" : nomeOriginal;
        int ponto = base.indexOf('.');
        if (ponto >= 0) {
            base = base.substring(0, ponto);
        }
        base = base.replaceAll("[^A-Za-z0-9_-]", "");
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }
        return base.isEmpty() ? "pet" : base;
    }
}
