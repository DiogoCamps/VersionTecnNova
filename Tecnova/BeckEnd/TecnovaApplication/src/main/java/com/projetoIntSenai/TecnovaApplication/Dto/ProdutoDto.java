package com.projetoIntSenai.TecnovaApplication.Dto;

import com.projetoIntSenai.TecnovaApplication.Entity.Imagem;
import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO (Data Transfer Object) para representar os dados de um Produto na comunicação com o cliente (frontend).
 * Recebe e envia URLs de imagens como Strings.
 */
public class ProdutoDto {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome não pode ter mais que 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição não pode ter mais que 500 caracteres")
    private String textoDescritivo;

    @NotBlank(message = "Fabricante é obrigatório")
    @Size(max = 100, message = "Fabricante não pode ter mais que 100 caracteres")
    private String fabricante;

    @NotBlank(message = "Cor é obrigatória")
    @Size(max = 50, message = "Cor não pode ter mais que 50 caracteres")
    private String cor;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private BigDecimal preco;

    @NotNull(message = "Quantidade é obrigatória")
    @PositiveOrZero(message = "Quantidade não pode ser negativa")
    private Integer quantidade;

    // Lista de URLs completas das imagens para o frontend
    private List<String> imagens;

    // Construtor padrão
    public ProdutoDto() {}

    // Construtor completo
    public ProdutoDto(Long id, String nome, String textoDescritivo, String fabricante, String cor, BigDecimal preco, Integer quantidade, List<String> imagens) {
        this.id = id;
        this.nome = nome;
        this.textoDescritivo = textoDescritivo;
        this.fabricante = fabricante;
        this.cor = cor;
        this.preco = preco;
        this.quantidade = quantidade;
        this.imagens = imagens;
    }

    /**
     * Converte uma entidade Produto para um ProdutoDto.
     * @param produto A entidade a ser convertida.
     * @return O DTO correspondente.
     */
    public static ProdutoDto fromEntity(Produto produto) {
        if (produto == null) {
            return null;
        }

        List<String> urlsImagens = Collections.emptyList();
        if (produto.getImagens() != null && !produto.getImagens().isEmpty()) {
            urlsImagens = produto.getImagens().stream()
                    // Constrói a URL completa para cada imagem
                    .map(imagem -> buildImageUrl(imagem.getNomeArquivo()))
                    .collect(Collectors.toList());
        }

        return new ProdutoDto(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getFabricante(),
                produto.getCor(),
                produto.getPreco(),
                produto.getQuantidade(),
                urlsImagens
        );
    }

    /**
     * Converte este ProdutoDto para uma entidade Produto.
     * @return A entidade Produto correspondente.
     */
    public Produto toEntity() {
        Produto produto = new Produto();
        produto.setId(this.id);
        produto.setNome(this.nome);
        produto.setDescricao(this.textoDescritivo); // Mapeia 'textoDescritivo' para 'descricao'
        produto.setFabricante(this.fabricante);
        produto.setCor(this.cor);
        produto.setPreco(this.preco);
        produto.setQuantidade(this.quantidade);
        // A lista de imagens não é populada aqui, pois ela é gerenciada pelo serviço ao salvar os arquivos.
        return produto;
    }

    /**
     * Helper para construir a URL da imagem.
     * ATENÇÃO: A URL base deve corresponder à configuração do seu servidor.
     * @param nomeArquivo O nome do arquivo da imagem.
     * @return A URL completa.
     */
    private static String buildImageUrl(String nomeArquivo) {
        // Exemplo: http://localhost:8080/api/produtos/imagens/nome-do-arquivo.jpg
        // Altere a base da URL se necessário.
        return "http://localhost:8080/api/produtos/imagens/" + nomeArquivo;
    }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTextoDescritivo() { return textoDescritivo; }
    public void setTextoDescritivo(String textoDescritivo) { this.textoDescritivo = textoDescritivo; }
    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public List<String> getImagens() { return imagens; }
    public void setImagens(List<String> imagens) { this.imagens = imagens; }
}