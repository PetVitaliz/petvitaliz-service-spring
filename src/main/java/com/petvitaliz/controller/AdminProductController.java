package com.petvitaliz.controller;

import com.petvitaliz.dto.ProductRequest;
import com.petvitaliz.dto.ProductResponse;
import com.petvitaliz.service.ProductService;
import com.petvitaliz.service.SubscriptionService;
import com.petvitaliz.web.JsonText;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/listar/produtos")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/cadastrar")
    public ResponseEntity<String> create(@RequestBody ProductRequest request) {
        productService.create(request);
        return JsonText.response(HttpStatus.CREATED, "Produto cadastrado com sucesso");
    }

    @GetMapping
    public Map<String, List<ProductResponse>> list() {
        return Map.of("produtos", productService.list());
    }

    @GetMapping("/{id}")
    public Map<String, ProductResponse> get(@PathVariable Integer id) {
        return Map.of("produto", productService.get(id));
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Integer id, @RequestBody ProductRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Produto atualizado com sucesso");
        body.put("novo_Produto", productService.update(id, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping("/assinaturas/cancelar")
    public Map<String, Object> cancelAllSubscriptions() {
        int canceled = subscriptionService.cancelAll();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", canceled == 0
                ? "Nenhum plano contratado para cancelar"
                : canceled + (canceled == 1 ? " plano contratado foi cancelado" : " planos contratados foram cancelados"));
        body.put("canceladas", canceled);
        return body;
    }

    @DeleteMapping("/excluir/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensagem", "Produto deletado com sucesso");
        body.put("Produtos", productService.delete(id));
        return body;
    }
}
