package com.petvitaliz.service;

import com.petvitaliz.dto.PetResponse;
import com.petvitaliz.entity.Pet;
import com.petvitaliz.enums.PetEspecie;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.PetRepository;
import com.petvitaliz.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinaryService;

    public PetService(PetRepository petRepository,
                      UsuarioRepository usuarioRepository,
                      CloudinaryService cloudinaryService) {
        this.petRepository = petRepository;
        this.usuarioRepository = usuarioRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public List<PetResponse> listar(Integer idUsuario) {
        return petRepository.findByUsuarioId(idUsuario).stream()
                .map(pet -> PetResponse.de(pet, idUsuario))
                .toList();
    }

    public void cadastrar(Integer idUsuario, PetForm form, MultipartFile imagem) {
        // Valida tudo primeiro: nao vale gastar um upload se o formulario esta errado.
        String nome = PetParser.nome(form.nome());
        Double peso = PetParser.pesoObrigatorio(form.peso());
        PetEspecie especie = PetParser.especie(form.especie());
        String outraEspecie = PetParser.outraEspecie(especie, form.outraEspecie());
        var sexo = PetParser.sexo(form.sexo());
        var nascimento = PetParser.dataNascimento(form.dataNascimento());
        int idade = PetParser.idadeOuZero(form.idade());
        String observacoes = PetParser.observacoes(form.observacoes());

        boolean temImagem = imagem != null && !imagem.isEmpty();
        if (temImagem) {
            cloudinaryService.validar(imagem);
        }
        String fotoUrl = temImagem ? cloudinaryService.enviarImagem(imagem) : null;

        Pet pet = new Pet();
        pet.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        pet.setNome(nome);
        pet.setEspecie(especie);
        pet.setOutraEspecie(outraEspecie);
        pet.setSexo(sexo);
        pet.setDataNascimento(nascimento);
        pet.setIdade(idade);
        pet.setPeso(peso);
        pet.setObservacoes(observacoes);
        pet.setFotoUrl(fotoUrl);
        petRepository.save(pet);
    }

    public PetResponse editar(Integer idUsuario, Integer idPet, PetForm form, MultipartFile imagem) {
        Pet pet = buscarDoUsuario(idUsuario, idPet);

        String nome = PetParser.nome(form.nome());
        PetEspecie especie = PetParser.especie(form.especie());
        String outraEspecie = PetParser.outraEspecie(especie, form.outraEspecie());
        var sexo = PetParser.sexo(form.sexo());
        var nascimento = PetParser.dataNascimento(form.dataNascimento());
        Double peso = PetParser.pesoOpcional(form.peso());
        String observacoes = PetParser.observacoes(form.observacoes());

        boolean temImagem = imagem != null && !imagem.isEmpty();
        if (temImagem) {
            cloudinaryService.validar(imagem);
        }

        pet.setNome(nome);
        pet.setEspecie(especie);
        pet.setOutraEspecie(outraEspecie);
        pet.setSexo(sexo);
        pet.setDataNascimento(nascimento);
        // Idade nao enviada = mantem a atual (no Express virava NaN e dava erro 500).
        if (form.idade() != null && !form.idade().isBlank()) {
            pet.setIdade(PetParser.idadeOuZero(form.idade()));
        }
        pet.setPeso(peso);
        pet.setObservacoes(observacoes);
        if (temImagem) {
            pet.setFotoUrl(cloudinaryService.enviarImagem(imagem));
        }

        petRepository.save(pet);
        return PetResponse.de(pet, idUsuario);
    }

    @Transactional
    public void excluir(Integer idUsuario, Integer idPet) {
        Pet pet = buscarDoUsuario(idUsuario, idPet);

        try {
            petRepository.delete(pet);
            petRepository.flush(); // forca o DELETE agora para pegar o erro de chave estrangeira aqui
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Este pet possui consultas e não pode ser excluído");
        }
    }

    private Pet buscarDoUsuario(Integer idUsuario, Integer idPet) {
        if (idPet == null || idPet <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Id inválido");
        }
        return petRepository.findByIdAndUsuarioId(idPet, idUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
    }
}
