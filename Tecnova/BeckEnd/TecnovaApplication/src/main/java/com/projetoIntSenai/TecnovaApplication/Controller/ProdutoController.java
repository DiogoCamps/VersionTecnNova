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

/**
 * Controlador REST para gerenciar operações relacionadas a produtos.
 * Lida com criação, leitura, atualização e exclusão (CRUD) de produtos,
 * incluindo o upload e o serviço de imagens.
 */
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "http://localhost:8080"}) // Permite requisições do seu frontend
public class ProdutoController {

    private final ProdutoService produtoService;
    private final FileStorageService fileStorageService;

    /**
     * Construtor para injeção de dependências.
     *
     * @param produtoService O serviço de lógica de negócios para produtos.
     * @param fileStorageService O serviço para manipulação de arquivos (imagens).
     */
    public ProdutoController(ProdutoService produtoService, FileStorageService fileStorageService) {
        this.produtoService = produtoService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Cria um novo produto no sistema.
     * Este endpoint consome 'multipart/form-data' para permitir o envio de dados do produto (JSON)
     * e um arquivo de imagem.
     *
     * @param produtoDto O DTO do produto com os dados a serem salvos. Validado automaticamente pelo Spring.
     * @param imagem O arquivo de imagem a ser associado ao produto (opcional).
     * @return ResponseEntity contendo o ProdutoDto criado e o status HTTP 201 Created.
     * @throws IOException Se houver um erro ao processar a imagem.
     * @throws IllegalArgumentException Se a imagem for inválida (ex: tamanho, tipo - se as validações forem reativadas).
     * Nota: Exceções de validação e outras RuntimeExceptions são tratadas pelo GlobalExceptionHandler.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDto> criarProduto(
            @Valid @RequestPart("produto") ProdutoDto produtoDto, // Recebe o JSON do produto como uma parte
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) throws IOException {

        // Se uma imagem foi fornecida, salva-a e define o nome do arquivo no DTO do produto
        if (imagem != null && !imagem.isEmpty()) {
            String nomeArquivo = fileStorageService.salvarImagem(imagem);
            produtoDto.setImagem(nomeArquivo); // Define o nome do arquivo da imagem no DTO
        } else {
            // Se nenhuma imagem foi fornecida, garante que o campo imagem no DTO seja nulo
            produtoDto.setImagem(null);
        }

        ProdutoDto produtoCriado = produtoService.criarProduto(produtoDto);
        return ResponseEntity.created(URI.create("/api/produtos/" + produtoCriado.getId()))
                .body(produtoCriado);
    }

    /**
     * Busca um produto pelo seu ID.
     *
     * @param id O ID do produto a ser buscado.
     * @return ResponseEntity contendo o ProdutoDto encontrado e o status HTTP 200 OK.
     * @throws ProdutoNotFoundException Se o produto com o ID especificado não for encontrado.
     * Nota: Exceções são tratadas pelo GlobalExceptionHandler.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDto> buscarProduto(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    /**
     * Lista todos os produtos ou filtra-os por nome.
     *
     * @param nome (Opcional) O nome ou parte do nome para filtrar os produtos.
     * @return ResponseEntity contendo uma lista de ProdutoDto e o status HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<ProdutoDto>> listarProdutos(
            @RequestParam(required = false) String nome) {
        List<ProdutoDto> produtos = (nome != null && !nome.isEmpty())
                ? produtoService.buscarPorNome(nome)
                : produtoService.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Serve um arquivo de imagem com base no seu nome.
     *
     * @param nomeArquivo O nome do arquivo da imagem a ser servido.
     * @return ResponseEntity contendo o Resource da imagem e o tipo de mídia adequado.
     * @throws IOException Se o arquivo não for encontrado ou houver um erro de leitura.
     * Nota: Exceções são tratadas pelo GlobalExceptionHandler.
     */
    @GetMapping("/imagem/{nomeArquivo}")
    public ResponseEntity<Resource> servirImagem(@PathVariable String nomeArquivo) throws IOException {
        Resource resource = fileStorageService.carregarImagem(nomeArquivo);
        String contentType = determinarContentType(nomeArquivo);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    /**
     * Atualiza um produto existente.
     * Este endpoint também consome 'multipart/form-data' e permite atualizar os dados do produto
     * e, opcionalmente, substituir a imagem existente.
     *
     * @param id O ID do produto a ser atualizado.
     * @param produtoDto O DTO do produto com os dados atualizados.
     * @param imagem O novo arquivo de imagem (opcional).
     * @return ResponseEntity contendo o ProdutoDto atualizado e o status HTTP 200 OK.
     * @throws ProdutoNotFoundException Se o produto com o ID especificado não for encontrado.
     * @throws IOException Se houver um erro ao processar a imagem.
     * @throws IllegalArgumentException Se a imagem for inválida.
     * Nota: Exceções são tratadas pelo GlobalExceptionHandler.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDto> atualizarProduto(
            @PathVariable Long id,
            @Valid @RequestPart("produto") ProdutoDto produtoDto, // Recebe o JSON do produto como uma parte
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) throws IOException {

        // Se uma nova imagem foi fornecida, salva-a e define o nome do arquivo no DTO do produto
        if (imagem != null && !imagem.isEmpty()) {
            String nomeArquivo = fileStorageService.salvarImagem(imagem);
            produtoDto.setImagem(nomeArquivo); // Define o nome do novo arquivo da imagem no DTO
        }
        // Nota: Se imagem for nula/vazia, o campo `imagem` do produtoDto manterá o valor que veio do frontend,
        // o que significa que o backend deve preservar a imagem existente se nada for enviado.
        // O `atualizarCamposProduto` no ProdutoService deve lidar com isso.

        ProdutoDto produtoAtualizado = produtoService.atualizarProduto(id, produtoDto);
        return ResponseEntity.ok(produtoAtualizado);
    }

    /**
     * Remove um produto pelo seu ID.
     *
     * @param id O ID do produto a ser removido.
     * @return ResponseEntity com status HTTP 204 No Content.
     * @throws ProdutoNotFoundException Se o produto com o ID especificado não for encontrado.
     * Nota: Exceções são tratadas pelo GlobalExceptionHandler.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Define o status HTTP 204 para sucesso na exclusão
    public void removerProduto(@PathVariable Long id) {
        produtoService.removerProduto(id);
    }

    /**
     * Determina o tipo de mídia (Content-Type) de um arquivo com base na sua extensão.
     * Isso é usado para servir corretamente as imagens.
     *
     * @param filename O nome do arquivo.
     * @return O tipo de mídia (MIME type) correspondente à extensão do arquivo.
     */
    private String determinarContentType(String filename) {
        String extension = "";
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            extension = filename.substring(dotIndex + 1).toLowerCase();
        }

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";    // Adicionado
            case "webp" -> "image/webp";   // Adicionado
            case "tiff", "tif" -> "image/tiff"; // Adicionado
            case "ico" -> "image/x-icon"; // Adicionado
            default -> "application/octet-stream"; // Tipo padrão para arquivos desconhecidos
        };
    }
}