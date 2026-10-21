package com.petvitaliz.controller;

import com.petvitaliz.dto.ContatoRequest;
import com.petvitaliz.dto.PerfilResponse;
import com.petvitaliz.dto.ProdutoResponse;
import com.petvitaliz.security.UsuarioToken;
import com.petvitaliz.service.ProdutoService;
import com.petvitaliz.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    private static final String AVISO_LOGIN = "faça login para utilizar esse recurso";

    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;

    public HomeController(ProdutoService produtoService, UsuarioService usuarioService) {
        this.produtoService = produtoService;
        this.usuarioService = usuarioService;
    }

    // parte NAO logada

    @GetMapping("/")
    public Map<String, Object> homeNaoLogada() {
        List<ProdutoResponse> produtos = produtoService.listar();

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pagina", "Home");
        corpo.put("versão", "não logada");
        if (produtos.isEmpty()) {
            corpo.put("aviso", "faça login para utilizar outros recursos.");
            corpo.put("produtos", "nenhum produto cadastrado no momento");
        } else {
            corpo.put("aviso", "faça login para utilizar outros recursos");
            corpo.put("produtos", produtos);
        }
        return corpo;
    }

    @GetMapping("/servicos")
    public Map<String, Object> servicosNaoLogado() {
        return paginaNaoLogada("Pagina de serviços", false);
    }

    @GetMapping("/contato")
    public Map<String, Object> contatoNaoLogado() {
        return paginaNaoLogada("Pagina de contato", true);
    }

    @GetMapping("/servicos-de-emergencia")
    public Map<String, Object> servicosEmergenciaNaoLogado() {
        return paginaNaoLogada("Pagina de serviços de emergencia", false);
    }

    @GetMapping("/listar/pet")
    public Map<String, Object> listarPetNaoLogado() {
        return paginaNaoLogada("Pagina de listar pet", true);
    }

    @GetMapping("/cadastar/pet")
    public Map<String, Object> cadastrarPetNaoLogado() {
        return paginaNaoLogada("Pagina de cadastrar pet", true);
    }

    @GetMapping("/editar/pet/{id}")
    public Map<String, Object> editarPetNaoLogado() {
        return paginaNaoLogada("Pagina de editar pet", true);
    }

    @GetMapping("/delete/pet/{id}")
    public Map<String, Object> excluirPetNaoLogado() {
        return paginaNaoLogada("Pagina de excluir pet", true);
    }

    // parte logada

    @GetMapping("/user/home")
    public Map<String, Object> home(@AuthenticationPrincipal UsuarioToken usuario) {
        return Map.of("pagina", "Home Logada", "usuario", nomeCompleto(usuario));
    }

    @GetMapping("/user/servicos")
    public Map<String, Object> servicos(@AuthenticationPrincipal UsuarioToken usuario) {
        return Map.of("pagina", "Serviços Logado", "usuario", nomeCompleto(usuario));
    }

    @GetMapping("/user/servicos-de-emergencia")
    public Map<String, Object> servicosEmergencia(@AuthenticationPrincipal UsuarioToken usuario) {
        return Map.of("mensagem", "Pagina de serviços de emergencia", "usuario", nomeCompleto(usuario));
    }

    @PostMapping("/user/contato")
    public Map<String, String> contato(@AuthenticationPrincipal UsuarioToken usuario,
                                       @Valid @RequestBody ContatoRequest req) {
        usuarioService.enviarContato(usuario, req);
        return Map.of("mensagem", "Email enviado com sucesso");
    }

    @GetMapping("/user/buscar/perfil")
    public PerfilResponse buscarPerfil(@AuthenticationPrincipal UsuarioToken usuario) {
        return usuarioService.buscarPerfil(usuario.id());
    }

    // helpers

    private static Map<String, Object> paginaNaoLogada(String pagina, boolean comAviso) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pagina", pagina);
        corpo.put("versão", "não logada");
        if (comAviso) {
            corpo.put("aviso", AVISO_LOGIN);
        }
        return corpo;
    }

    private static String nomeCompleto(UsuarioToken usuario) {
        return usuario.nome() + " " + usuario.sobrenome();
    }
}
