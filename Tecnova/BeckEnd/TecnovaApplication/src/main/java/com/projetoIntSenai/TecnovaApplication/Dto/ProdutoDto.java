package com.projetoIntSenai.TecnovaApplication.Dto;

import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidade;
    private String imagem;  // caminho da imagem no servidor

    // Método para converter Produto para ProdutoDTO
    public static ProdutoDto fromEntity(Produto produto) {
        ProdutoDto dto = new ProdutoDto();
        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setPreco(produto.getPreco());
        dto.setQuantidade(produto.getQuantidade());
        dto.setImagem(produto.getImagem());
        return dto;
    }

    // Método para converter ProdutoDTO para Produto
    public Produto toEntity() {
        Produto produto = new Produto();
        produto.setId(this.getId());
        produto.setNome(this.getNome());
        produto.setDescricao(this.getDescricao());
        produto.setPreco(this.getPreco());
        produto.setQuantidade(this.getQuantidade());
        produto.setImagem(this.getImagem());
        return produto;
    }
}
