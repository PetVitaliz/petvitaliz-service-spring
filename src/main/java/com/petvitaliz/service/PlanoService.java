package com.petvitaliz.service;

import com.petvitaliz.dto.ProdutoResponse;
import com.petvitaliz.entity.Assinatura;
import com.petvitaliz.entity.Produto;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.AssinaturaRepository;
import com.petvitaliz.repository.ProdutoRepository;
import com.petvitaliz.security.UsuarioToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PlanoService {

    private static final Logger log = LoggerFactory.getLogger(PlanoService.class);

    private final AssinaturaRepository assinaturaRepository;
    private final ProdutoRepository produtoRepository;
    private final EmailService emailService;

    public PlanoService(AssinaturaRepository assinaturaRepository,
                        ProdutoRepository produtoRepository,
                        EmailService emailService) {
        this.assinaturaRepository = assinaturaRepository;
        this.produtoRepository = produtoRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public Optional<ProdutoResponse> buscarPlanoAtual(Integer idUsuario) {
        return assinaturaRepository.findFirstByIdUsuario(idUsuario)
                .map(assinatura -> ProdutoResponse.de(assinatura.getProduto()));
    }

    @Transactional
    public void assinar(UsuarioToken usuario, Integer idProduto) {
        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Id não encontrado"));

        if (assinaturaRepository.findFirstByIdUsuario(usuario.id()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Você já possui um plano, cancele o plano atual para assinar outro");
        }

        Assinatura assinatura = new Assinatura();
        assinatura.setIdUsuario(usuario.id());
        assinatura.setProduto(produto);
        assinaturaRepository.save(assinatura);

        try {
            emailService.enviarPlanoAssinado(
                    usuario.nome(), usuario.sobrenome(), usuario.email(),
                    produto.getNome(), produto.getPreco());
        } catch (RuntimeException e) {
            log.error("Plano assinado, mas o e-mail de confirmacao falhou", e);
        }
    }

    @Transactional
    public void cancelar(Integer idUsuario) {
        Assinatura assinatura = assinaturaRepository.findFirstByIdUsuario(idUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Você não possui nenhum plano para ser cancelado"));

        assinaturaRepository.delete(assinatura);
    }
}
