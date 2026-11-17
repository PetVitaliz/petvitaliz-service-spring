package com.petvitaliz.service;

import com.petvitaliz.dto.ContactRequest;
import com.petvitaliz.dto.ProfileResponse;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.UserRepository;
import com.petvitaliz.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Integer userId) {
        return userRepository.findById(userId)
                .map(ProfileResponse::of)
                .orElseThrow(() -> ApiException.notFound("Usuário não encontrado"));
    }

    public void sendContact(AuthenticatedUser user, ContactRequest request) {
        try {
            emailService.sendContact(user.fullName(), user.email(), request.email().trim(), request.message());
        } catch (RuntimeException e) {
            log.error("Failed to send contact e-mail", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar o email");
        }
    }
}
