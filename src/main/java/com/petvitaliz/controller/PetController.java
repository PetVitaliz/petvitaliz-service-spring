package com.petvitaliz.controller;

import com.petvitaliz.dto.MessageResponse;
import com.petvitaliz.dto.PetForm;
import com.petvitaliz.dto.PetResponse;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.service.PetService;
import com.petvitaliz.web.JsonText;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user/listar/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public Map<String, Object> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of(
                "mensagem", "Pagina de listar pet",
                "usuario", user.fullName(),
                "pets", petService.list(user.id()));
    }

    @PostMapping(value = "/cadastar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createFromForm(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @ModelAttribute PetForm form,
                                                 @RequestParam(name = "image", required = false) MultipartFile image) {
        petService.create(user.id(), form, image);
        return JsonText.response(HttpStatus.CREATED, "Pet cadastrado com sucesso");
    }

    @PostMapping(value = "/cadastar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createFromJson(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @RequestBody PetForm form) {
        return createFromForm(user, form, null);
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, PetResponse> updateFromForm(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @PathVariable Integer id,
                                                   @ModelAttribute PetForm form,
                                                   @RequestParam(name = "image", required = false) MultipartFile image) {
        return Map.of("pet", petService.update(user.id(), id, form, image));
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, PetResponse> updateFromJson(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @PathVariable Integer id,
                                                   @RequestBody PetForm form) {
        return updateFromForm(user, id, form, null);
    }

    @DeleteMapping("/delete/{id}")
    public MessageResponse delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Integer id) {
        petService.delete(user.id(), id);
        return new MessageResponse("Pet deletado com sucesso");
    }
}
