package com.projetoIntSenai.TecnovaApplication.Controller;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Exceptions.ProdutoNotFoundException;
import com.projetoIntSenai.TecnovaApplication.Service.FileStorageService;
import com.projetoIntSenai.TecnovaApplication.Service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;
    private final FileStorageService fileStorageService;

    public ProdutoController(ProdutoService produtoService, FileStorageService fileStorageService) {
        this.produtoService = produtoService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> criarProduto(
            @Valid @RequestPart("produto") ProdutoDto produtoDto,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) {

        try {
            if (imagem != null && !imagem.isEmpty()) {
                String nomeArquivo = fileStorageService.salvarImagem(imagem);
                produtoDto.setImagem(nomeArquivo);
            }

            ProdutoDto produtoCriado = produtoService.criarProduto(produtoDto);
            return ResponseEntity.created(URI.create("/api/produtos/" + produtoCriado.getId()))
                    .body(produtoCriado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar imagem: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarProduto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(produtoService.buscarPorId(id));
        } catch (ProdutoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProdutoDto>> listarProdutos(
            @RequestParam(required = false) String nome) {
        List<ProdutoDto> produtos = (nome != null && !nome.isEmpty())
                ? produtoService.buscarPorNome(nome)
                : produtoService.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/imagem/{nomeArquivo}")
    public ResponseEntity<?> servirImagem(@PathVariable String nomeArquivo) {
        try {
            Resource resource = fileStorageService.carregarImagem(nomeArquivo);
            String contentType = determinarContentType(nomeArquivo);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> atualizarProduto(
            @PathVariable Long id,
            @Valid @RequestPart("produto") ProdutoDto produtoDto,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) {

        try {
            if (imagem != null && !imagem.isEmpty()) {
                String nomeArquivo = fileStorageService.salvarImagem(imagem);
                produtoDto.setImagem(nomeArquivo);
            }

            return ResponseEntity.ok(produtoService.atualizarProduto(id, produtoDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar imagem: " + e.getMessage());
        } catch (ProdutoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerProduto(@PathVariable Long id) {
        try {
            produtoService.removerProduto(id);
            return ResponseEntity.noContent().build();
        } catch (ProdutoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determinarContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}