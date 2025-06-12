package com.projetoIntSenai.TecnovaApplication.Service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    private final String uploadDir = "uploads"; // Nome do diretório de upload

    public FileStorageService() throws RuntimeException {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            // Cria o diretório se ele não existir
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao criar diretório de uploads: " + this.fileStorageLocation, ex);
        }
    }

    /**
     * Salva um arquivo MultipartFile no sistema de arquivos e retorna o nome único gerado.
     * @param file O arquivo a ser salvo.
     * @return O nome único do arquivo salvo.
     * @throws IOException Se ocorrer um erro de I/O durante o salvamento.
     */
    public String salvarArquivo(MultipartFile file) throws IOException {
        validarArquivoPresente(file);
        String nomeArquivo = gerarNomeArquivoUnico(file);
        Path targetLocation = this.fileStorageLocation.resolve(nomeArquivo);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return nomeArquivo;
    }

    /**
     * Carrega um arquivo do sistema de arquivos como um Resource.
     * @param nomeArquivo O nome do arquivo a ser carregado.
     * @return O Resource do arquivo.
     * @throws FileNotFoundException Se o arquivo não for encontrado ou não puder ser lido.
     */
    public Resource carregarArquivoComoRecurso(String nomeArquivo) throws FileNotFoundException {
        try {
            Path filePath = this.fileStorageLocation.resolve(nomeArquivo).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new FileNotFoundException("Arquivo não encontrado ou não pode ser lido: " + nomeArquivo);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("Erro de URL ao carregar arquivo: " + nomeArquivo + " - " + ex.getMessage());
        }
    }

    /**
     * Deleta um arquivo do sistema de arquivos.
     * @param nomeArquivo O nome do arquivo a ser deletado.
     * @return true se o arquivo foi deletado com sucesso, false caso contrário.
     * @throws IOException Se ocorrer um erro de I/O durante a deleção.
     */
    public boolean deletarArquivo(String nomeArquivo) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(nomeArquivo).normalize();
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            return true;
        }
        return false; // Arquivo não existia
    }

    private void validarArquivoPresente(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo não pode ser nulo ou vazio.");
        }
    }

    private String gerarNomeArquivoUnico(MultipartFile file) {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String extensao = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extensao = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extensao.toLowerCase();
    }
}