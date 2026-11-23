package com.petvitaliz.service;

import com.petvitaliz.dto.ContactRequest;
import com.petvitaliz.dto.ProfileResponse;
import com.petvitaliz.dto.ProfileUpdateRequest;
import com.petvitaliz.entity.User;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.UserRepository;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.util.DateParser;
import com.petvitaliz.util.Texts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Integer userId) {
        return userRepository.findById(userId)
                .map(ProfileResponse::of)
                .orElseThrow(() -> ApiException.notFound("Usuário não encontrado"));
    }

    @Transactional
    public ProfileResponse updateProfile(Integer userId, ProfileUpdateRequest request) {
        String firstName = Texts.trimToEmpty(request.firstName());
        if (firstName.length() < 2) {
            throw ApiException.badRequest("Nome é obrigatório e deve ter pelo menos 2 caracteres");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuário não encontrado"));

        String phone = Texts.trimToEmpty(request.phone()).replaceAll("\\D", "");
        if (!phone.isEmpty()) {
            if (userRepository.existsByPhoneAndIdNot(phone, userId)) {
                throw ApiException.conflict("Número de telefone já cadastrado");
            }
            user.setPhone(phone);
        }

        if (request.birthDate() != null && !request.birthDate().isBlank()) {
            LocalDate birthDate = DateParser.parse(request.birthDate())
                    .orElseThrow(() -> ApiException.badRequest("Data de nascimento inválida"));
            if (birthDate.isAfter(LocalDate.now(clock))) {
                throw ApiException.badRequest("A data de nascimento não pode ser no futuro");
            }
            user.setBirthDate(birthDate);
        }

        user.setFirstName(firstName);
        user.setLastName(Texts.trimToEmpty(request.lastName()));
        return ProfileResponse.of(user);
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
