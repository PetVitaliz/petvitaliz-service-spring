package com.petvitaliz.controller;

import com.petvitaliz.dto.AdminForm;
import com.petvitaliz.dto.AdminResponse;
import com.petvitaliz.security.AuthenticatedUser;
import com.petvitaliz.service.AdminAccountService;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/listar/adm")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @PostMapping(value = "/cadastrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createFromForm(@ModelAttribute AdminForm form,
                                                 @RequestParam(name = "image", required = false) MultipartFile image) {
        adminAccountService.create(form, image);
        return JsonText.response(HttpStatus.CREATED, "Administrador cadastrado com sucesso");
    }

    @PostMapping(value = "/cadastrar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createFromJson(@RequestBody AdminForm form) {
        return createFromForm(form, null);
    }

    @GetMapping
    public Map<String, List<AdminResponse>> list() {
        return Map.of("ADMs", adminAccountService.list());
    }

    @GetMapping("/{id}")
    public Map<String, List<AdminResponse>> get(@PathVariable Integer id) {
        return Map.of("ADMs", List.of(adminAccountService.get(id)));
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> updateFromForm(@PathVariable Integer id,
                                              @ModelAttribute AdminForm form,
                                              @RequestParam(name = "image", required = false) MultipartFile image) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Administrador editado com sucesso");
        body.put("ADM", adminAccountService.update(id, form, image));
        return body;
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> updateFromJson(@PathVariable Integer id, @RequestBody AdminForm form) {
        return updateFromForm(id, form, null);
    }

    @DeleteMapping("/excluir/{id}")
    public Map<String, Object> delete(@AuthenticationPrincipal AuthenticatedUser admin, @PathVariable Integer id) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Administrador deletado com sucesso");
        body.put("ADMs", adminAccountService.delete(admin.id(), id));
        return body;
    }
}
