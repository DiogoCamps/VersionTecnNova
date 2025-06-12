package com.projetoIntSenai.TecnovaApplication.Entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidade que representa a imagem de um produto.
 * Armazena o nome do arquivo único gerado para a imagem salva no sistema de arquivos.
 */
@Entity
@Table(name = "imagens_produto")
public class Imagem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Armazena o nome único do arquivo (ex: "uuid-aleatorio.jpg")
    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    // Construtor padrão exigido pela JPA
    public Imagem() {}

    // Construtor para facilitar a criação
    public Imagem(String nomeArquivo, Produto produto) {
        this.nomeArquivo = nomeArquivo;
        this.produto = produto;
    }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
    public Integer getOrdemExibicao() { return ordemExibicao; }
    public void setOrdemExibicao(Integer ordemExibicao) { this.ordemExibicao = ordemExibicao; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
}