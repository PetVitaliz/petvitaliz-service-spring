package com.petvitaliz.dto;

import com.petvitaliz.entity.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoResponseTest {

    private static ProdutoResponse comPreco(String preco) {
        Produto produto = new Produto();
        produto.setNome("Plano Teste");
        produto.setDescricao("Descricao");
        produto.setPreco(new BigDecimal(preco));
        return ProdutoResponse.de(produto);
    }

    @Test
    void precoSaiComoTextoSemZerosSobrando() {
        assertThat(comPreco("99.90").preco()).isEqualTo("99.9");
        assertThat(comPreco("149.99").preco()).isEqualTo("149.99");
        assertThat(comPreco("100.00").preco()).isEqualTo("100");
    }
}
