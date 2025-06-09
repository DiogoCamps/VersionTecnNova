package com.projetoIntSenai.TecnovaApplication.Controller;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Exceptions.ProdutoNotFoundException;
import com.projetoIntSenai.TecnovaApplication.Service.FileStorageService;
import com.projetoIntSenai.TecnovaApplication.Service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProdutoController(ProdutoService produtoService, FileStorageService fileStorageService) {
        this.produtoService = produtoService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDto> criarProduto(
            @Valid @RequestPart("produto") ProdutoDto produtoDto,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) {

        try {
            if (imagem != null && !imagem.isEmpty()) {
                String caminhoImagem = fileStorageService.salvarImagem(imagem);
                produtoDto.setImagem(caminhoImagem);
            }

            ProdutoDto produtoCriado = produtoService.criarProduto(produtoDto);
            return new ResponseEntity<>(produtoCriado, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDto> buscarProduto(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoDto>> listarProdutos(
            @RequestParam(required = false) String nome) {

        List<ProdutoDto> produtos = (nome != null && !nome.isEmpty())
                ? produtoService.buscarPorNome(nome)
                : produtoService.listarTodos();

        return ResponseEntity.ok(produtos);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDto> atualizarProduto(
            @PathVariable Long id,
            @Valid @RequestPart("produto") ProdutoDto produtoDto,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) {

        try {
            if (imagem != null && !imagem.isEmpty()) {
                String caminhoImagem = fileStorageService.salvarImagem(imagem);
                produtoDto.setImagem(caminhoImagem);
            }

            return ResponseEntity.ok(produtoService.atualizarProduto(id, produtoDto));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (ProdutoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerProduto(@PathVariable Long id) {
        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }
}