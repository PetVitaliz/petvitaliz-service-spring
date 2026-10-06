package com.petvitaliz.service;

import com.petvitaliz.dto.AlterarSenhaRequest;
import com.petvitaliz.dto.CadastroRequest;
import com.petvitaliz.dto.ConfirmarCodigoRequest;
import com.petvitaliz.dto.LoginRequest;
import com.petvitaliz.dto.LoginResultado;
import com.petvitaliz.entity.Administrador;
import com.petvitaliz.entity.Funcionario;
import com.petvitaliz.entity.ResetSenha;
import com.petvitaliz.entity.Usuario;
import com.petvitaliz.enums.UsuarioGenero;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.AdministradorRepository;
import com.petvitaliz.repository.FuncionarioRepository;
import com.petvitaliz.repository.ResetSenhaRepository;
import com.petvitaliz.repository.UsuarioRepository;
import com.petvitaliz.security.JwtService;
import com.petvitaliz.security.TipoToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final int MAX_TENTATIVAS_CODIGO = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ResetSenhaRepository resetSenhaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final Map<String, Integer> tentativasCodigo = new ConcurrentHashMap<>();

    public AuthService(UsuarioRepository usuarioRepository,
                       AdministradorRepository administradorRepository,
                       FuncionarioRepository funcionarioRepository,
                       ResetSenhaRepository resetSenhaRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.administradorRepository = administradorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.resetSenhaRepository = resetSenhaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    //  CADASTRO

    public void cadastrar(CadastroRequest req) {
        String email = normalizar(req.email());
        String cpf = req.cpf().trim();
        String telefone = req.telefone().trim();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email já cadastrado");
        }
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CPF já cadastrado");
        }
        if (usuarioRepository.existsByTelefone(telefone)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Número de telefone já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setSobrenome(req.sobrenome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        usuario.setCpf(cpf);
        usuario.setTelefone(telefone);
        usuario.setGenero(UsuarioGenero.valueOf(req.genero().trim().toUpperCase()));
        usuario.setDataNascimento(lerData(req.dataNascimento()));
        usuarioRepository.save(usuario);
    }

    // LOGINS

    public LoginResultado loginUsuario(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(req.email()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email invalido"));

        if (!passwordEncoder.matches(req.senha(), usuario.getSenha())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Senha invalida");
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", usuario.getId());
        claims.put("email", usuario.getEmail());
        claims.put("nome", usuario.getNome());
        claims.put("sobrenome", usuario.getSobrenome());

        Map<String, Object> dadosUsuario = new LinkedHashMap<>();
        dadosUsuario.put("nome", usuario.getNome() + " " + usuario.getSobrenome());
        dadosUsuario.put("email", usuario.getEmail());
        dadosUsuario.put("tipo", "usuario");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", "Login realizado com sucesso");
        corpo.put("usuario", dadosUsuario);

        return new LoginResultado(gerarToken(TipoToken.USUARIO, claims), corpo);
    }

    public LoginResultado loginAdm(LoginRequest req) {
        Administrador adm = administradorRepository.findByEmail(normalizar(req.email()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email invalido"));

        if (!passwordEncoder.matches(req.senha(), adm.getSenha())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Senha invalida");
        }
        if (!Boolean.TRUE.equals(adm.getAtivo())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuário não está mais ativo");
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", adm.getId());
        claims.put("email", adm.getEmail());
        claims.put("nome", adm.getNome());
        claims.put("ativo", adm.getAtivo());
        claims.put("foto_url", adm.getFotoUrl());

        Map<String, Object> dadosUsuario = new LinkedHashMap<>();
        dadosUsuario.put("id", adm.getId());
        dadosUsuario.put("nome", adm.getNome());
        dadosUsuario.put("email", adm.getEmail());
        dadosUsuario.put("foto_url", adm.getFotoUrl());
        dadosUsuario.put("tipo", "admin");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", "Login como ADM realizado com sucesso");
        corpo.put("usuario", dadosUsuario);

        return new LoginResultado(gerarToken(TipoToken.ADMINISTRADOR, claims), corpo);
    }

    public LoginResultado loginFuncionario(LoginRequest req) {
        Funcionario funcionario = funcionarioRepository.findByEmail(normalizar(req.email()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email invalido"));

        if (!passwordEncoder.matches(req.senha(), funcionario.getSenha())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Senha invalida");
        }
        if (!Boolean.TRUE.equals(funcionario.getAtivo())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esse usuario não esta mais ativo");
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", funcionario.getId());
        claims.put("email", funcionario.getEmail());
        claims.put("nome", funcionario.getNome());
        claims.put("sobrenome", funcionario.getSobrenome());
        claims.put("ativo", funcionario.getAtivo());
        claims.put("especialidade", funcionario.getEspecialidade().name());

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("message", "Login realizado com sucesso");
        corpo.put("nome", funcionario.getNome());

        return new LoginResultado(gerarToken(TipoToken.FUNCIONARIO, claims), corpo);
    }

    // RESET SENHA

    @Transactional
    public void pedirResetSenha(String emailInformado) {
        String email = normalizar(emailInformado);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "email não encontrado"));

        resetSenhaRepository.findFirstByUsuarioIdOrderByExpiraEmDesc(usuario.getId()).ifPresent(ultimo -> {
            if (ultimo.getExpiraEm().isAfter(LocalDateTime.now().plusMinutes(9))) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                        "Aguarde um minuto antes de pedir outro código");
            }
        });

        resetSenhaRepository.deleteByUsuarioId(usuario.getId());

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));

        ResetSenha reset = new ResetSenha();
        reset.setUsuario(usuario);
        reset.setToken(codigo);
        reset.setExpiraEm(LocalDateTime.now().plusMinutes(10));
        resetSenhaRepository.save(reset);

        try {
            emailService.enviarCodigoReset(usuario.getNome(), usuario.getSobrenome(), email, codigo);
        } catch (RuntimeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível enviar o e-mail agora. Tente novamente em instantes");
        }

        tentativasCodigo.remove(email);
    }

    @Transactional
    public LoginResultado confirmarCodigo(ConfirmarCodigoRequest req) {
        String email = normalizar(req.email());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Codigo invalido"));

        if (tentativasCodigo.getOrDefault(email, 0) >= MAX_TENTATIVAS_CODIGO) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas. Peça um novo código");
        }

        ResetSenha reset = resetSenhaRepository.findFirstByUsuarioIdOrderByExpiraEmDesc(usuario.getId())
                .filter(r -> r.getExpiraEm().isAfter(LocalDateTime.now()))
                .filter(r -> codigosIguais(r.getToken(), req.codigo().trim()))
                .orElse(null);

        if (reset == null) {
            tentativasCodigo.merge(email, 1, Integer::sum);
            throw new ApiException(HttpStatus.NOT_FOUND, "Codigo invalido");
        }

        resetSenhaRepository.delete(reset);
        tentativasCodigo.remove(email);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", usuario.getId());
        claims.put("reset_autorizado", true);
        String token = gerarToken(TipoToken.RESET, claims);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", "Acesso liberado");
        corpo.put("token_reset", token);

        return new LoginResultado(token, corpo);
    }

    @Transactional
    public void alterarSenha(Integer idUsuario, AlterarSenhaRequest req) {
        if (!req.senha1().equals(req.senha2())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "As senhas não estão iguais");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(req.senha1()));
        usuarioRepository.save(usuario);
    }

    // HELPERS

    private String gerarToken(TipoToken tipo, Map<String, Object> claims) {
        Map<String, Object> todos = new LinkedHashMap<>(claims);
        todos.put("role", tipo.getRole());
        todos.values().removeIf(Objects::isNull);
        return jwtService.gerar(todos, tipo.getValidade());
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase();
    }

    private static boolean codigosIguais(String esperado, String informado) {
        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                informado.getBytes(StandardCharsets.UTF_8));
    }

    private static LocalDate lerData(String texto) {
        String data = texto.trim();
        try {
            return LocalDate.parse(data.substring(0, Math.min(10, data.length())));
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Data de nascimento inválida (use AAAA-MM-DD)");
        }
    }
}
