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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif"
    );

    public FileStorageService() throws RuntimeException {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao criar diretório de uploads", ex);
        }
    }

    public String salvarImagem(MultipartFile imagem) throws IOException {
        validarImagem(imagem);
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

    private void validarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem não pode ser vazio");
        }
        if (imagem.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Tamanho máximo excedido (5MB)");
        }
        String contentType = imagem.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Tipo de arquivo inválido. Use JPEG, PNG ou GIF");
        }
    }

    private String gerarNomeArquivoUnico(MultipartFile imagem) {
        String originalFilename = Objects.requireNonNull(imagem.getOriginalFilename());
        String extensao = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".bin";
        return UUID.randomUUID() + extensao.toLowerCase();
    }
}