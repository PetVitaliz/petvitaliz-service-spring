package com.petvitaliz.controller;

import com.petvitaliz.dto.EmployeeForm;
import com.petvitaliz.dto.EmployeeResponse;
import com.petvitaliz.service.AdminEmployeeService;
import com.petvitaliz.web.JsonText;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/adm/listar/funcionario")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @PostMapping(value = "/cadastrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createFromForm(@ModelAttribute EmployeeForm form,
                                                 @RequestParam(name = "image", required = false) MultipartFile image) {
        adminEmployeeService.create(form, image);
        return JsonText.response(HttpStatus.CREATED, "Funcionário cadastrado com sucesso");
    }

    @PostMapping(value = "/cadastrar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createFromJson(@RequestBody EmployeeForm form) {
        return createFromForm(form, null);
    }

    @GetMapping
    public Map<String, List<EmployeeResponse>> list() {
        return Map.of("funcionarios", adminEmployeeService.list());
    }

    @GetMapping("/{id}")
    public Map<String, EmployeeResponse> get(@PathVariable Integer id) {
        return Map.of("funcionario", adminEmployeeService.get(id));
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> updateFromForm(@PathVariable Integer id,
                                              @ModelAttribute EmployeeForm form,
                                              @RequestParam(name = "image", required = false) MultipartFile image) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Funcionário editado com sucesso");
        body.put("funcionario", adminEmployeeService.update(id, form, image));
        return body;
    }

    @PutMapping(value = "/editar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> updateFromJson(@PathVariable Integer id, @RequestBody EmployeeForm form) {
        return updateFromForm(id, form, null);
    }

    @DeleteMapping("/excluir/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Funcionario deletado com sucesso");
        body.put("funcionarios", adminEmployeeService.delete(id));
        return body;
    }
}
