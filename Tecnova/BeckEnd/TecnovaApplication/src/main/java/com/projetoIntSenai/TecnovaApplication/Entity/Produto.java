package com.projetoIntSenai.TecnovaApplication.Entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produtos")
public class Produto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500) // Campo 'descricao' na Entity
    private String descricao;

    @Column(length = 100) // Adicionado campo fabricante
    private String fabricante;

    @Column(length = 50) // Adicionado campo cor
    private String cor;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer quantidade = 0; // Valor padrão

    // Relação One-to-Many com a nova entidade Imagem
    // mappedBy: Indica o campo na entidade Imagem que possui a relação (produto)
    // CascadeType.ALL: Operações no produto (salvar, atualizar, excluir) propagam para as imagens associadas
    // orphanRemoval = true: Se uma imagem for removida da lista 'imagens' do produto, ela será excluída do banco
    // fetch = FetchType.LAZY: Carrega as imagens apenas quando a lista é acessada (otimização)
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Imagem> imagens = new ArrayList<>(); // Inicializa a lista para evitar NullPointerException

    // Campos de auditoria
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Callback para atualizar 'dataAtualizacao' antes de cada atualização no banco
    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Construtor padrão (necessário para JPA)
    public Produto() {
    }

    // Construtor para criação de Produto (sem ID e sem imagens inicialmente)
    public Produto(String nome, String descricao, String fabricante, String cor, BigDecimal preco, Integer quantidade) {
        this.nome = nome;
        this.descricao = descricao;
        this.fabricante = fabricante;
        this.cor = cor;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    // Construtor completo (geralmente usado ao carregar do banco)
    public Produto(Long id, String nome, String descricao, String fabricante, String cor, BigDecimal preco, Integer quantidade, List<Imagem> imagens) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.fabricante = fabricante;
        this.cor = cor;
        this.preco = preco;
        this.quantidade = quantidade;
        // Adiciona as imagens e garante a relação bidirecional
        if (imagens != null) {
            this.imagens.addAll(imagens);
            for (Imagem img : imagens) {
                img.setProduto(this);
            }
        }
    }
    // Construtor com ID (para edição/carregamento sem a lista de imagens, que será populada pelo JPA)
    public Produto(Long id, String nome, String descricao, String fabricante, String cor, BigDecimal preco, Integer quantidade) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.fabricante = fabricante;
        this.cor = cor;
        this.preco = preco;
        this.quantidade = quantidade;
    }


    // --- Getters e Setters ---

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

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
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

    // Getter e Setter para a lista de imagens
    public List<Imagem> getImagens() {
        return imagens;
    }

    public void setImagens(List<Imagem> imagens) {
        this.imagens.clear(); // Limpa a lista atual
        if (imagens != null) {
            this.imagens.addAll(imagens); // Adiciona todas as novas imagens
            // Garante a relação bidirecional
            for (Imagem imagem : imagens) {
                imagem.setProduto(this);
            }
        }
    }

    // Métodos auxiliares para adicionar/remover imagens individualmente
    public void addImagem(Imagem imagem) {
        if (!this.imagens.contains(imagem)) { // Evita duplicatas
            this.imagens.add(imagem);
            imagem.setProduto(this);
        }
    }

    public void removeImagem(Imagem imagem) {
        if (this.imagens.contains(imagem)) {
            this.imagens.remove(imagem);
            imagem.setProduto(null); // Desvincula a imagem do produto
        }
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}