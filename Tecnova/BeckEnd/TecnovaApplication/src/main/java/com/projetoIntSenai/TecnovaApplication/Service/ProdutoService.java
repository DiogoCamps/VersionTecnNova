package com.projetoIntSenai.TecnovaApplication.Service;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import com.projetoIntSenai.TecnovaApplication.Exceptions.ProdutoNotFoundException;
import com.projetoIntSenai.TecnovaApplication.Repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository, FileStorageService fileStorageService) {
        this.produtoRepository = produtoRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ProdutoDto criarProduto(ProdutoDto produtoDto) {
        Produto produto = produtoDto.toEntity();
        Produto produtoSalvo = produtoRepository.save(produto);
        return ProdutoDto.fromEntity(produtoSalvo);
    }

    @Transactional
    public ProdutoDto atualizarProduto(Long id, ProdutoDto produtoDto) {
        return produtoRepository.findById(id)
                .map(produtoExistente -> {
                    atualizarCamposProduto(produtoExistente, produtoDto);
                    Produto produtoAtualizado = produtoRepository.save(produtoExistente);
                    return ProdutoDto.fromEntity(produtoAtualizado);
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
    public ProdutoDto atualizarProdutoComImagem(Long id, ProdutoDto produtoDto, MultipartFile imagem) throws IOException {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));

        atualizarCamposProduto(produto, produtoDto);

        if (imagem != null && !imagem.isEmpty()) {
            String nomeArquivo = fileStorageService.salvarImagem(imagem);
            produto.setImagem(nomeArquivo);
        }

        Produto produtoAtualizado = produtoRepository.save(produto);
        return ProdutoDto.fromEntity(produtoAtualizado);
    }

    private void atualizarCamposProduto(Produto produto, ProdutoDto produtoDto) {
        produto.setNome(produtoDto.getNome());
        produto.setDescricao(produtoDto.getDescricao());
        produto.setPreco(produtoDto.getPreco());
        produto.setQuantidade(produtoDto.getQuantidade());

        // Mantém a imagem existente se não for fornecida nova
        if (produtoDto.getImagem() != null) {
            produto.setImagem(produtoDto.getImagem());
        }
    }

    @Transactional
    public void removerImagemDoProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));

        if (produto.getImagem() != null && !produto.getImagem().isEmpty()) {
            try {
                // Implemente a lógica para remover o arquivo físico se necessário
                // fileStorageService.removerImagem(produto.getImagem());
                produto.setImagem(null);
                produtoRepository.save(produto);
            } catch (Exception e) {
                throw new RuntimeException("Falha ao remover imagem do produto", e);
            }
        }
    }
}