package com.projetoIntSenai.TecnovaApplication.Dto;

import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class ProdutoDto {
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome não pode ter mais que 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição não pode ter mais que 500 caracteres")
    private String descricao;

    @Positive(message = "Preço deve ser positivo")
    @Digits(integer = 13, fraction = 2, message = "Preço deve ter no máximo 13 dígitos inteiros e 2 decimais")
    private BigDecimal preco;

    @PositiveOrZero(message = "Quantidade não pode ser negativa")
    private Integer quantidade;

    private String imagem;

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

    // Melhorar métodos de conversão
    public static ProdutoDto fromEntity(Produto produto) {
        return new ProdutoDto(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidade(),
                produto.getImagem()
        );
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

    public Produto toEntity() {
        return new Produto(
                this.id,
                this.nome,
                this.descricao,
                this.preco,
                this.quantidade,
                this.imagem
        );
    }
}