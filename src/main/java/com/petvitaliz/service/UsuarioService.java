package com.petvitaliz.service;

import com.petvitaliz.dto.ContatoRequest;
import com.petvitaliz.dto.PerfilResponse;
import com.petvitaliz.entity.Usuario;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.UsuarioRepository;
import com.petvitaliz.security.UsuarioToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    public PerfilResponse buscarPerfil(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        return new PerfilResponse(usuario.getNome(), usuario.getSobrenome(), usuario.getEmail(), usuario.getCpf());
    }

    public void enviarContato(UsuarioToken usuario, ContatoRequest req) {
        try {
            emailService.enviarContato(
                    usuario.nome(), usuario.sobrenome(), usuario.email(),
                    req.email().trim(), req.mensagem());
        } catch (RuntimeException e) {
            log.error("Erro ao enviar e-mail de contato", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar o email");
        }
    }
}
