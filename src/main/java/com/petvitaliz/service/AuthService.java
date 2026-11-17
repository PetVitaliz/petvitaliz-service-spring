package com.petvitaliz.service;

import com.petvitaliz.dto.LoginRequest;
import com.petvitaliz.dto.LoginResult;
import com.petvitaliz.dto.NewPasswordRequest;
import com.petvitaliz.dto.ResetCodeRequest;
import com.petvitaliz.dto.SignupRequest;
import com.petvitaliz.entity.Admin;
import com.petvitaliz.entity.Employee;
import com.petvitaliz.entity.PasswordReset;
import com.petvitaliz.entity.User;
import com.petvitaliz.enums.Gender;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.AdminRepository;
import com.petvitaliz.repository.EmployeeRepository;
import com.petvitaliz.repository.PasswordResetRepository;
import com.petvitaliz.repository.UserRepository;
import com.petvitaliz.security.JwtClaims;
import com.petvitaliz.security.JwtService;
import com.petvitaliz.security.TokenType;
import com.petvitaliz.util.DateParser;
import com.petvitaliz.util.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_CODE_ATTEMPTS = 5;
    private static final int CODE_LIFETIME_MINUTES = 10;
    private static final int CODE_RESEND_WAIT_MINUTES = 1;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final Map<String, Integer> codeAttempts = new ConcurrentHashMap<>();

    public void signUp(SignupRequest request) {
        String email = Texts.normalizeEmail(request.email());
        String cpf = request.cpf().trim();
        String phone = request.phone().trim();

        if (userRepository.existsByEmail(email)) {
            throw ApiException.badRequest("Email já cadastrado");
        }
        if (userRepository.existsByCpf(cpf)) {
            throw ApiException.badRequest("CPF já cadastrado");
        }
        if (userRepository.existsByPhone(phone)) {
            throw ApiException.badRequest("Número de telefone já cadastrado");
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCpf(cpf);
        user.setPhone(phone);
        user.setGender(Gender.valueOf(request.gender().trim().toUpperCase()));
        user.setBirthDate(DateParser.parse(request.birthDate()).orElseThrow(() ->
                ApiException.badRequest("Data de nascimento inválida (use AAAA-MM-DD)")));
        userRepository.save(user);
    }

    public LoginResult loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(Texts.normalizeEmail(request.email()))
                .orElseThrow(AuthService::invalidEmail);
        requirePassword(request.password(), user.getPassword());

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(JwtClaims.ID, user.getId());
        claims.put(JwtClaims.EMAIL, user.getEmail());
        claims.put(JwtClaims.FIRST_NAME, user.getFirstName());
        claims.put(JwtClaims.LAST_NAME, user.getLastName());

        Map<String, Object> userData = new LinkedHashMap<>();
        userData.put("nome", Texts.fullName(user.getFirstName(), user.getLastName()));
        userData.put("email", user.getEmail());
        userData.put("tipo", "usuario");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Login realizado com sucesso");
        body.put("usuario", userData);

        return new LoginResult(jwtService.generate(TokenType.USER, claims), body);
    }

    public LoginResult loginAdmin(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(Texts.normalizeEmail(request.email()))
                .orElseThrow(AuthService::invalidEmail);
        requirePassword(request.password(), admin.getPassword());
        if (!Boolean.TRUE.equals(admin.getActive())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuário não está mais ativo");
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(JwtClaims.ID, admin.getId());
        claims.put(JwtClaims.EMAIL, admin.getEmail());
        claims.put(JwtClaims.FIRST_NAME, admin.getUsername());

        Map<String, Object> userData = new LinkedHashMap<>();
        userData.put("id", admin.getId());
        userData.put("nome", admin.getUsername());
        userData.put("email", admin.getEmail());
        userData.put("foto_url", admin.getPhotoUrl());
        userData.put("tipo", "admin");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Login como ADM realizado com sucesso");
        body.put("usuario", userData);

        return new LoginResult(jwtService.generate(TokenType.ADMIN, claims), body);
    }

    public LoginResult loginEmployee(LoginRequest request) {
        Employee employee = employeeRepository.findByEmail(Texts.normalizeEmail(request.email()))
                .orElseThrow(AuthService::invalidEmail);
        requirePassword(request.password(), employee.getPassword());
        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esse usuario não esta mais ativo");
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(JwtClaims.ID, employee.getId());
        claims.put(JwtClaims.EMAIL, employee.getEmail());
        claims.put(JwtClaims.FIRST_NAME, employee.getFirstName());
        claims.put(JwtClaims.LAST_NAME, employee.getLastName());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Login realizado com sucesso");
        body.put("nome", employee.getFirstName());

        return new LoginResult(jwtService.generate(TokenType.EMPLOYEE, claims), body);
    }

    @Transactional
    public void requestPasswordReset(String rawEmail) {
        String email = Texts.normalizeEmail(rawEmail);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("email não encontrado"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resendThreshold = now.plusMinutes(CODE_LIFETIME_MINUTES - CODE_RESEND_WAIT_MINUTES);
        passwordResetRepository.findFirstByUserIdOrderByExpiresAtDesc(user.getId())
                .filter(latest -> latest.getExpiresAt().isAfter(resendThreshold))
                .ifPresent(latest -> {
                    throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                            "Aguarde um minuto antes de pedir outro código");
                });

        passwordResetRepository.deleteByUserId(user.getId());

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        PasswordReset reset = new PasswordReset();
        reset.setUser(user);
        reset.setCode(code);
        reset.setExpiresAt(now.plusMinutes(CODE_LIFETIME_MINUTES));
        passwordResetRepository.save(reset);

        try {
            emailService.sendPasswordResetCode(Texts.fullName(user.getFirstName(), user.getLastName()), email, code);
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar o e-mail agora. Tente novamente em instantes");
        }

        codeAttempts.remove(email);
    }

    @Transactional
    public LoginResult confirmResetCode(ResetCodeRequest request) {
        String email = Texts.normalizeEmail(request.email());
        User user = userRepository.findByEmail(email).orElseThrow(AuthService::invalidCode);

        if (codeAttempts.getOrDefault(email, 0) >= MAX_CODE_ATTEMPTS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Peça um novo código");
        }

        PasswordReset reset = passwordResetRepository.findFirstByUserIdOrderByExpiresAtDesc(user.getId())
                .filter(latest -> latest.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(latest -> sameCode(latest.getCode(), request.code().trim()))
                .orElse(null);

        if (reset == null) {
            codeAttempts.merge(email, 1, Integer::sum);
            throw invalidCode();
        }

        passwordResetRepository.delete(reset);
        codeAttempts.remove(email);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(JwtClaims.ID, user.getId());
        claims.put(JwtClaims.RESET_AUTHORIZED, true);
        String token = jwtService.generate(TokenType.PASSWORD_RESET, claims);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Acesso liberado");
        body.put("token_reset", token);

        return new LoginResult(token, body);
    }

    @Transactional
    public void changePassword(Integer userId, NewPasswordRequest request) {
        if (!request.password().equals(request.confirmation())) {
            throw ApiException.badRequest("As senhas não estão iguais");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Usuário não encontrado"));
        user.setPassword(passwordEncoder.encode(request.password()));
    }

    private void requirePassword(String rawPassword, String hash) {
        if (!passwordEncoder.matches(rawPassword, hash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Senha invalida");
        }
    }

    private static ApiException invalidEmail() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Email invalido");
    }

    private static ApiException invalidCode() {
        return ApiException.notFound("Codigo invalido");
    }

    private static boolean sameCode(String expected, String provided) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }
}
