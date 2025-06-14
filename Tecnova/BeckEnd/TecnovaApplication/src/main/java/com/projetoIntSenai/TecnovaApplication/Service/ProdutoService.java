package com.projetoIntSenai.TecnovaApplication.Service;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Entity.Imagem;
import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import com.projetoIntSenai.TecnovaApplication.Repository.ImagemRepository;
import com.projetoIntSenai.TecnovaApplication.Repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemRepository imagemRepository; // Embora não usado diretamente, é bom ter para futuras operações com imagens

    @Autowired
    private FileStorageService fileStorageService;

    public List<Produto> findAll() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> findById(Long id) {
        return produtoRepository.findById(id);
    }

    @Transactional
    public Produto salvarProdutoComImagens(ProdutoDto produtoDto, List<MultipartFile> imagens) throws IOException {
        Produto produto = produtoDto.toEntity();

        if (imagens != null && !imagens.isEmpty()) {
            for (MultipartFile imagemFile : imagens) {
                if (imagemFile != null && !imagemFile.isEmpty()) {
                    String nomeArquivo = fileStorageService.salvarArquivo(imagemFile);
                    Imagem novaImagem = new Imagem(nomeArquivo, produto);
                    produto.addImagem(novaImagem);
                }
            }
        }

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarProdutoComImagens(Long id, ProdutoDto produtoDto, List<MultipartFile> novasImagens) throws IOException {
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        produtoExistente.setNome(produtoDto.getNome());
        produtoExistente.setDescricao(produtoDto.getTextoDescritivo());
        produtoExistente.setFabricante(produtoDto.getFabricante());
        produtoExistente.setCor(produtoDto.getCor());
        produtoExistente.setPreco(produtoDto.getPreco());
        produtoExistente.setQuantidade(produtoDto.getQuantidade());

        if (novasImagens != null && !novasImagens.isEmpty()) {
            for (MultipartFile imagemFile : novasImagens) {
                if (imagemFile != null && !imagemFile.isEmpty()) {
                    String nomeArquivo = fileStorageService.salvarArquivo(imagemFile);
                    Imagem novaImagem = new Imagem(nomeArquivo, produtoExistente);
                    produtoExistente.addImagem(novaImagem);
                }
            }
        }

        return produtoRepository.save(produtoExistente);
    }

    @Transactional
    public void deleteProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        if (produto.getImagens() != null) {
            for (Imagem imagem : produto.getImagens()) {
                try {
                    fileStorageService.deletarArquivo(imagem.getNomeArquivo());
                } catch (IOException e) {
                    System.err.println("Erro ao deletar arquivo de imagem do disco: " + imagem.getNomeArquivo() + " para o produto " + id + ". Erro: " + e.getMessage());
                }
            }
        }

        produtoRepository.delete(produto);
    }

    public Resource carregarImagem(String nomeArquivo) throws FileNotFoundException {
        return fileStorageService.carregarArquivoComoRecurso(nomeArquivo);
    }

    @Transactional
    public List<Produto> importarProdutosDeJson(List<ProdutoDto> produtosDto) throws IOException {
        List<Produto> produtosImportados = new ArrayList<>();

        for (ProdutoDto dto : produtosDto) {
            List<MultipartFile> imagensParaUpload = new ArrayList<>();

            if (dto.getImagens() != null && !dto.getImagens().isEmpty()) {
                for (String imageUrl : dto.getImagens()) {
                    try {
                        byte[] imageBytes = downloadImageFromUrl(imageUrl);
                        String originalFilename = getFilenameFromUrl(imageUrl);

                        MultipartFile mockFile = new MockMultipartFile(
                                "file", originalFilename, "image/jpeg", imageBytes
                        );
                        imagensParaUpload.add(mockFile);
                    } catch (IOException e) {
                        System.err.println("Erro ao baixar imagem da URL: " + imageUrl + " para o produto " + dto.getNome() + ". Pulando esta imagem.");
                    }
                }
            }

            Produto produtoSalvo = this.salvarProdutoComImagens(dto, imagensParaUpload);
            produtosImportados.add(produtoSalvo);
        }
        return produtosImportados;
    }

    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    private String getFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
