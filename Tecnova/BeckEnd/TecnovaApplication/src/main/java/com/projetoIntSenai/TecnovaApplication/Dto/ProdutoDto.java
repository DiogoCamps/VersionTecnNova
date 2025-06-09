package com.projetoIntSenai.TecnovaApplication.Dto;

import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class ProdutoDto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidade;
    private String imagem;  // caminho da imagem no servidor

    public ProdutoDto() {

    }

    public ProdutoDto(Long id, String nome, String descricao, BigDecimal preco, Integer quantidade, String imagem) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidade = quantidade;
        this.imagem = imagem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

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
