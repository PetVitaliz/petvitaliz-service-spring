package com.petvitaliz.dto;

import com.petvitaliz.entity.Produto;

public record ProdutoResponse(String nome, String descricao, String beneficios, String preco) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getNome(),
                produto.getDescricao(),
                produto.getBeneficios(),
                produto.getPreco().stripTrailingZeros().toPlainString());
    }
}
