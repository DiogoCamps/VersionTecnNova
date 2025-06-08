package com.projetoIntSenai.TecnovaApplication.Service;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import com.projetoIntSenai.TecnovaApplication.Repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    // Criar produto
    public ProdutoDto criarProduto(ProdutoDto dto) {
        Produto produto = dto.toEntity();
        Produto salvo = produtoRepository.save(produto);
        return ProdutoDto.fromEntity(salvo);
    }

    // Atualizar produto
    public ProdutoDto atualizarProduto(Long id, ProdutoDto dto) {
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

        produtoExistente.setNome(dto.getNome());
        produtoExistente.setDescricao(dto.getDescricao());
        produtoExistente.setPreco(dto.getPreco());
        produtoExistente.setQuantidade(dto.getQuantidade());
        produtoExistente.setImagem(dto.getImagem());

        Produto atualizado = produtoRepository.save(produtoExistente);
        return ProdutoDto.fromEntity(atualizado);
    }

    // Buscar produto por id
    public ProdutoDto buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
        return ProdutoDto.fromEntity(produto);
    }

    // Listar todos os produtos
    public List<ProdutoDto> listarTodos() {
        List<Produto> produtos = produtoRepository.findAll();
        return produtos.stream()
                .map(ProdutoDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Remover produto
    public void removerProduto(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado com id: " + id);
        }
        produtoRepository.deleteById(id);
    }

    // Buscar produtos por nome (filtro)
    public List<ProdutoDto> buscarPorNome(String nome) {
        List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCase(nome);
        return produtos.stream()
                .map(ProdutoDto::fromEntity)
                .collect(Collectors.toList());
    }
}
