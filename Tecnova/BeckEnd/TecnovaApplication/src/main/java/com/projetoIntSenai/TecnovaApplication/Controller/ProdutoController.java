// Pacote: com.projetoIntSenai.TecnovaApplication.Controller
// Arquivo: ProdutoController.java

package com.projetoIntSenai.TecnovaApplication.Controller;

import com.projetoIntSenai.TecnovaApplication.Dto.ProdutoDto;
import com.projetoIntSenai.TecnovaApplication.Entity.Produto;
import com.projetoIntSenai.TecnovaApplication.Service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<ProdutoDto>> getAllProdutos() {
        List<Produto> produtos = produtoService.findAll();
        List<ProdutoDto> dtos = produtos.stream()
                .map(ProdutoDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDto> getProdutoById(@PathVariable Long id) {
        return produtoService.findById(id)
                .map(ProdutoDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para criar um único produto com upload de arquivos
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDto> createProduto(
            @RequestPart("produto") @Valid ProdutoDto produtoDto,
            @RequestPart(value = "imagens", required = false) List<MultipartFile> imagens) throws IOException {
        Produto produtoSalvo = produtoService.salvarProdutoComImagens(produtoDto, imagens);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoDto.fromEntity(produtoSalvo));
    }

    // NOVO ENDPOINT PARA IMPORTAÇÃO EM MASSA USANDO JSON COM URLs
    @PostMapping("/importar")
    public ResponseEntity<?> importarProdutos(@RequestBody List<ProdutoDto> produtosParaImportar) {
        try {
            List<Produto> produtosSalvos = produtoService.importarProdutosDeJson(produtosParaImportar);
            List<ProdutoDto> dtos = produtosSalvos.stream()
                    .map(ProdutoDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao importar produtos: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDto> updateProduto(
            @PathVariable Long id,
            @RequestPart("produto") @Valid ProdutoDto produtoDto,
            @RequestPart(value = "novasImagens", required = false) List<MultipartFile> novasImagens) throws IOException {
        Produto produtoAtualizado = produtoService.atualizarProdutoComImagens(id, produtoDto, novasImagens);
        return ResponseEntity.ok(ProdutoDto.fromEntity(produtoAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduto(@PathVariable Long id) {
        produtoService.deleteProduto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/imagens/{nomeArquivo:.+}")
    public ResponseEntity<Resource> getImagem(@PathVariable String nomeArquivo) {
        try {
            Resource resource = produtoService.carregarImagem(nomeArquivo);
            String contentType = "application/octet-stream";
            try {
                contentType = Files.probeContentType(resource.getFile().toPath());
            } catch (IOException e) {
                System.err.println("Não foi possível determinar o tipo do conteúdo para: " + nomeArquivo);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
