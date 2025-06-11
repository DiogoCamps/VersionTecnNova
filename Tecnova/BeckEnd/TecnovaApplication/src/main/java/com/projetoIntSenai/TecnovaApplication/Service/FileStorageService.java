package com.projetoIntSenai.TecnovaApplication.Service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    // As constantes MAX_FILE_SIZE e ALLOWED_CONTENT_TYPES foram removidas.

    public FileStorageService() throws RuntimeException {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao criar diretório de uploads", ex);
        }
    }

    public String salvarImagem(MultipartFile imagem) throws IOException {
        // A validação de imagem foi simplificada para apenas verificar se não é nula/vazia.
        validarArquivoPresente(imagem); // Renomeei o método para refletir a validação simplificada
        String nomeArquivo = gerarNomeArquivoUnico(imagem);
        Path targetLocation = this.fileStorageLocation.resolve(nomeArquivo);
        Files.copy(imagem.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return nomeArquivo;
    }

    public Resource carregarImagem(String nomeArquivo) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(nomeArquivo).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("Imagem não encontrada ou não pode ser lida: " + nomeArquivo);
        }
        return resource;
    }

    // Método de validação simplificado, sem restrições de tamanho ou tipo.
    private void validarArquivoPresente(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem não pode ser vazio.");
        }
    }

    private String gerarNomeArquivoUnico(MultipartFile imagem) {
        String originalFilename = Objects.requireNonNull(imagem.getOriginalFilename());
        String extensao = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ""; // Alterado para string vazia em vez de ".bin" se não houver extensão
        return UUID.randomUUID().toString() + extensao.toLowerCase(); // Adicionado .toString() para o UUID
    }
}