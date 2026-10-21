package com.petvitaliz.controller;

import com.petvitaliz.dto.PetResponse;
import com.petvitaliz.security.UsuarioToken;
import com.petvitaliz.service.PetForm;
import com.petvitaliz.service.PetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user/listar/pet")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public Map<String, Object> listar(@AuthenticationPrincipal UsuarioToken usuario) {
        return Map.of(
                "mensagem", "Pagina de listar pet",
                "usuario", usuario.nome() + " " + usuario.sobrenome(),
                "pets", petService.listar(usuario.id()));
    }

    // (o "cadastar" sem "r" e igual ao da API Express: o frontend usa esse caminho)
    @PostMapping(value = "/cadastar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> cadastrar(@AuthenticationPrincipal UsuarioToken usuario,
                                            @RequestParam(required = false) String nome,
                                            @RequestParam(required = false) String especie,
                                            @RequestParam(name = "outra_especie", required = false) String outraEspecie,
                                            @RequestParam(required = false) String sexo,
                                            @RequestParam(name = "data_nascimento", required = false) String dataNascimento,
                                            @RequestParam(required = false) String idade,
                                            @RequestParam(required = false) String peso,
                                            @RequestParam(required = false) String observacoes,
                                            @RequestParam(name = "image", required = false) MultipartFile imagem) {
        petService.cadastrar(usuario.id(),
                new PetForm(nome, especie, outraEspecie, sexo, dataNascimento, idade, peso, observacoes),
                imagem);

        // O Express respondia res.json("texto"): um JSON que e so uma string, com aspas.
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("\"Pet cadastrado com sucesso\"");
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, PetResponse> editar(@AuthenticationPrincipal UsuarioToken usuario,
                                           @PathVariable Integer id,
                                           @RequestParam(required = false) String nome,
                                           @RequestParam(required = false) String especie,
                                           @RequestParam(name = "outra_especie", required = false) String outraEspecie,
                                           @RequestParam(required = false) String sexo,
                                           @RequestParam(name = "data_nascimento", required = false) String dataNascimento,
                                           @RequestParam(required = false) String idade,
                                           @RequestParam(required = false) String peso,
                                           @RequestParam(required = false) String observacoes,
                                           @RequestParam(name = "image", required = false) MultipartFile imagem) {
        PetResponse pet = petService.editar(usuario.id(), id,
                new PetForm(nome, especie, outraEspecie, sexo, dataNascimento, idade, peso, observacoes),
                imagem);
        return Map.of("pet", pet);
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, String> excluir(@AuthenticationPrincipal UsuarioToken usuario,
                                       @PathVariable Integer id) {
        petService.excluir(usuario.id(), id);
        return Map.of("mensagem", "Pet deletado com sucesso");
    }
}
