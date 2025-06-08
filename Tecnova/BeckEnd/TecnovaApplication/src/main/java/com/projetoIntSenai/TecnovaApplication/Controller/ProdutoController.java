package com.projetoIntSenai.TecnovaApplication.Controller;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // Pasta onde as imagens serão salvas (pode configurar depois)
    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping
    public ResponseEntity<ProdutoDto> criarProduto(@RequestPart("produto") ProdutoDto produtoDto,
                                                   @RequestPart(value = "imagem", required = false) MultipartFile imagem) throws IOException {
        if (imagem != null && !imagem.isEmpty()) {
            String caminhoImagem = salvarImagem(imagem);
            produtoDto.setImagem(caminhoImagem);
        }
        ProdutoDto criado = produtoService.criarProduto(produtoDto);
        return new ResponseEntity<>(criado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoDto> atualizarProduto(@PathVariable Long id,
                                                       @RequestPart("produto") ProdutoDto produtoDto,
                                                       @RequestPart(value = "imagem", required = false) MultipartFile imagem) throws IOException {
        if (imagem != null && !imagem.isEmpty()) {
            String caminhoImagem = salvarImagem(imagem);
            produtoDto.setImagem(caminhoImagem);
        }
        ProdutoDto atualizado = produtoService.atualizarProduto(id, produtoDto);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDto> buscarPorId(@PathVariable Long id) {
        ProdutoDto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoDto>> listarTodos(@RequestParam(required = false) String nome) {
        List<ProdutoDto> produtos;
        if (nome != null && !nome.isEmpty()) {
            produtos = produtoService.buscarPorNome(nome);
        } else {
            produtos = produtoService.listarTodos();
        }
        return ResponseEntity.ok(produtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerProduto(@PathVariable Long id) {
        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }

    // Método para salvar a imagem no servidor e retornar o caminho relativo
    private String salvarImagem(MultipartFile imagem) throws IOException {
        // Cria a pasta se não existir
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Nome do arquivo + limpeza para evitar caracteres problemáticos
        String nomeArquivo = StringUtils.cleanPath(imagem.getOriginalFilename());

        Path caminhoArquivo = Paths.get(UPLOAD_DIR + nomeArquivo);
        Files.copy(imagem.getInputStream(), caminhoArquivo);

        // Retorna o caminho relativo para salvar no banco, ex: "uploads/nome.jpg"
        return caminhoArquivo.toString();
    }
}
