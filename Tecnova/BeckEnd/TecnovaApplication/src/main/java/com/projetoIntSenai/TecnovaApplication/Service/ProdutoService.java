package com.projetoIntSenai.TecnovaApplication.Service;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import com.projetoIntSenai.TecnovaApplication.Exceptions.ProdutoNotFoundException;
import com.projetoIntSenai.TecnovaApplication.Repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoDto criarProduto(ProdutoDto dto) {
        Produto produto = dto.toEntity();
        Produto salvo = produtoRepository.save(produto);
        return ProdutoDto.fromEntity(salvo);
    }

    @Transactional
    public ProdutoDto atualizarProduto(Long id, ProdutoDto dto) {
        return produtoRepository.findById(id)
                .map(produtoExistente -> {
                    produtoExistente.setNome(dto.getNome());
                    produtoExistente.setDescricao(dto.getDescricao());
                    produtoExistente.setPreco(dto.getPreco());
                    produtoExistente.setQuantidade(dto.getQuantidade());
                    produtoExistente.setImagem(dto.getImagem());

                    Produto atualizado = produtoRepository.save(produtoExistente);
                    return ProdutoDto.fromEntity(atualizado);
                })
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public ProdutoDto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .map(ProdutoDto::fromEntity)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> listarTodos() {
        return produtoRepository.findAll().stream()
                .map(ProdutoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removerProduto(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new ProdutoNotFoundException(id);
        }
        produtoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(ProdutoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoDto atualizarComImagem(Long id, ProdutoDto dto, MultipartFile imagem) {
        // Implementação alternativa que pode ser usada pelo controller
        // Se preferir manter a separação de responsabilidades
        return produtoRepository.findById(id)
                .map(produto -> {
                    produto.setNome(dto.getNome());
                    produto.setDescricao(dto.getDescricao());
                    produto.setPreco(dto.getPreco());
                    produto.setQuantidade(dto.getQuantidade());

                    if (imagem != null && !imagem.isEmpty()) {
                        String nomeArquivo = gerarNomeArquivoUnico(imagem);
                        String caminhoImagem = salvarImagem(imagem, nomeArquivo);
                        produto.setImagem(caminhoImagem);
                    }

                    return ProdutoDto.fromEntity(produtoRepository.save(produto));
                })
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    private String gerarNomeArquivoUnico(MultipartFile imagem) {
        return UUID.randomUUID() + "_" + imagem.getOriginalFilename();
    }

    private String salvarImagem(MultipartFile imagem, String nomeArquivo) {
        // Implementação do salvamento da imagem
        // Pode ser movida para uma classe utilitária separada
        // Retorna o caminho relativo ou URL da imagem salva
        return "/imagens/" + nomeArquivo;
    }
}