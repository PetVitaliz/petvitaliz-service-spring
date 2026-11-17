package com.petvitaliz.service;

import com.petvitaliz.dto.AdminForm;
import com.petvitaliz.dto.AdminResponse;
import com.petvitaliz.entity.Admin;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.AdminRepository;
import com.petvitaliz.util.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private static final int MIN_USERNAME_LENGTH = 2;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public void create(AdminForm form, MultipartFile image) {
        String username = Texts.trimToEmpty(form.username());
        String email = Texts.normalizeEmail(form.email());

        if (username.length() < MIN_USERNAME_LENGTH) {
            throw invalid("username é obrigatorio e deve ter pelo menos 2 caracteres");
        }
        if (email.isEmpty()) {
            throw invalid("email é obrigatorio");
        }
        if (form.password() == null || form.password().length() < MIN_PASSWORD_LENGTH) {
            throw invalid("senha é obrigatorio e deve ter pelo menos 6 caracteres");
        }
        if (adminRepository.existsByEmail(email)) {
            throw invalid("Email ja cadastrado");
        }
        if (adminRepository.existsByUsername(username)) {
            throw invalid("Username ja cadastrado");
        }
        cloudinaryService.validate(image);

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setActive(Texts.isTrue(form.active()));
        admin.setPassword(passwordEncoder.encode(form.password()));
        admin.setPhotoUrl(cloudinaryService.uploadOrKeep(image, CloudinaryService.DEFAULT_FOLDER, null));
        adminRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> list() {
        return adminRepository.findAll().stream().map(AdminResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public AdminResponse get(Integer id) {
        return AdminResponse.of(find(id));
    }

    public AdminResponse update(Integer id, AdminForm form, MultipartFile image) {
        Admin admin = find(id);
        String username = Texts.trimToEmpty(form.username());
        String email = Texts.normalizeEmail(form.email());

        if (username.length() < MIN_USERNAME_LENGTH) {
            throw invalid("Username é obrigatorio");
        }
        if (email.isEmpty()) {
            throw invalid("Email é obrigatorio");
        }
        if (adminRepository.existsByEmailAndIdNot(email, id)) {
            throw invalid("Email ja cadastrado");
        }
        if (adminRepository.existsByUsernameAndIdNot(username, id)) {
            throw invalid("Username ja cadastrado");
        }
        cloudinaryService.validate(image);

        admin.setUsername(username);
        admin.setEmail(email);
        admin.setActive(Texts.isTrue(form.active()));
        String newPassword = Texts.trimToEmpty(form.password());
        if (newPassword.length() >= MIN_PASSWORD_LENGTH) {
            admin.setPassword(passwordEncoder.encode(newPassword));
        }
        admin.setPhotoUrl(cloudinaryService.uploadOrKeep(image, CloudinaryService.DEFAULT_FOLDER, admin.getPhotoUrl()));
        return AdminResponse.of(adminRepository.save(admin));
    }

    @Transactional
    public List<AdminResponse> delete(Integer currentAdminId, Integer id) {
        Admin admin = find(id);
        if (admin.getId().equals(currentAdminId)) {
            throw ApiException.conflict("Você não pode excluir a própria conta");
        }
        adminRepository.delete(admin);
        adminRepository.flush();
        return list();
    }

    private Admin find(Integer id) {
        return adminRepository.findById(id).orElseThrow(() -> ApiException.notFound("Id não encontrado"));
    }

    private static ApiException invalid(String message) {
        return ApiException.plain(HttpStatus.BAD_REQUEST, message);
    }
}
