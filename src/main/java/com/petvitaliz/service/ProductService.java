package com.petvitaliz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.petvitaliz.dto.ProductRequest;
import com.petvitaliz.dto.ProductResponse;
import com.petvitaliz.entity.Product;
import com.petvitaliz.exception.ApiException;
import com.petvitaliz.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int MIN_NAME_LENGTH = 5;
    private static final int MIN_BENEFITS_LENGTH = 6;
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> listForCatalog() {
        return productRepository.findAll().stream().map(ProductResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return productRepository.findAll().stream().map(ProductResponse::withId).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Integer id) {
        return ProductResponse.withId(find(id));
    }

    @Transactional
    public void create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse update(Integer id, ProductRequest request) {
        Product product = find(id);
        apply(product, request);
        return ProductResponse.withId(product);
    }

    @Transactional
    public List<ProductResponse> delete(Integer id) {
        Product product = find(id);
        PersistenceGuard.deleteOrConflict(() -> {
            productRepository.delete(product);
            productRepository.flush();
        }, "Este plano possui assinaturas e não pode ser excluído");

        return productRepository.findAllByOrderByIdDesc().stream().map(ProductResponse::withId).toList();
    }

    private Product find(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> ApiException.notFound("Id não encontrado"));
    }

    private static void apply(Product product, ProductRequest request) {
        if (request.name() == null || request.name().length() < MIN_NAME_LENGTH) {
            throw invalid("Nome é obrigatorio e deve ter pelo menos 5 caracteres");
        }
        if (request.description() == null || request.description().isBlank()) {
            throw invalid("Descrição é obrigatoria");
        }
        if (request.benefits() == null || request.benefits().length() < MIN_BENEFITS_LENGTH) {
            throw invalid("Beneficios é obrigatorio e deve ter pelo menos 6 caracteres");
        }

        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setBenefits(request.benefits());
        product.setPrice(parsePrice(request.price()));
    }

    private static BigDecimal parsePrice(JsonNode price) {
        if (price == null || price.isNull()) {
            throw invalid("Preço é obrigatorio");
        }
        if (!price.isNumber()) {
            throw invalid("Preço deve ser um numero");
        }
        BigDecimal value = price.decimalValue();
        if (value.signum() < 0 || value.compareTo(MAX_PRICE) > 0) {
            throw invalid("Preço deve ser um numero");
        }
        return value;
    }

    private static ApiException invalid(String message) {
        return ApiException.plain(HttpStatus.BAD_REQUEST, message);
    }
}
