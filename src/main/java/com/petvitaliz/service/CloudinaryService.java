package com.petvitaliz.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.petvitaliz.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class CloudinaryService {

    public static final String DEFAULT_FOLDER = "petvitaliz";
    public static final String EMPLOYEE_FOLDER = "petvitaliz_funcionarios";

    static final long MAX_FILE_SIZE = 4L * 1024 * 1024;

    private static final Set<String> ACCEPTED_TYPES = Set.of("image/jpeg", "image/png", "image/jpg");
    private static final int MAX_NAME_LENGTH = 50;

    private final Cloudinary cloudinary;
    private final boolean configured;

    public CloudinaryService(@Value("${app.cloudinary.cloud-name:}") String cloudName,
                             @Value("${app.cloudinary.api-key:}") String apiKey,
                             @Value("${app.cloudinary.api-secret:}") String apiSecret) {
        this.configured = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    public void validate(MultipartFile file) {
        if (!hasFile(file)) {
            return;
        }
        if (!ACCEPTED_TYPES.contains(String.valueOf(file.getContentType()).toLowerCase(Locale.ROOT))) {
            throw ApiException.badRequest("Tipo de arquivo inválido. Apenas JPEG, PNG e JPG são aceitos");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw ApiException.badRequest("A imagem deve ter no máximo 4MB");
        }
    }

    public String uploadOrKeep(MultipartFile file, String folder, String currentUrl) {
        return hasFile(file) ? upload(file, folder) : currentUrl;
    }

    public String upload(MultipartFile file, String folder) {
        validate(file);

        if (!configured) {
            log.error("Cloudinary is not configured: set CLOUDINARY_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET");
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", System.currentTimeMillis() + "-" + safeName(file.getOriginalFilename()),
                    "resource_type", "image"));

            Object url = result.get("secure_url");
            if (url == null) {
                throw new IllegalStateException("Cloudinary response has no secure_url");
            }
            return url.toString();
        } catch (IOException | RuntimeException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar a imagem agora. Tente novamente em instantes");
        }
    }

    static String safeName(String originalName) {
        String base = originalName == null ? "" : originalName;
        int dot = base.indexOf('.');
        if (dot >= 0) {
            base = base.substring(0, dot);
        }
        base = base.replaceAll("[^A-Za-z0-9_-]", "");
        if (base.length() > MAX_NAME_LENGTH) {
            base = base.substring(0, MAX_NAME_LENGTH);
        }
        return base.isEmpty() ? "pet" : base;
    }
}
