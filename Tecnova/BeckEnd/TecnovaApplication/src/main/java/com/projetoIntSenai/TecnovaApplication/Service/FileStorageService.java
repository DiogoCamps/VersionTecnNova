package com.projetoIntSenai.TecnovaApplication.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    // Diretório onde as imagens serão salvas
    private static final String UPLOAD_DIR = "uploads/";

    // Método principal para salvar uma imagem
    public String salvarImagem(MultipartFile imagem) throws IOException {
        // Gera um nome único para o arquivo
        String nomeArquivo = gerarNomeArquivoUnico(imagem);

        // Cria o diretório se não existir
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Salva o arquivo no sistema
        Path caminhoArquivo = uploadPath.resolve(nomeArquivo);
        Files.copy(imagem.getInputStream(), caminhoArquivo);

        // Retorna o caminho onde o arquivo foi salvo
        return caminhoArquivo.toString();
    }

    // Gera um nome único para evitar conflitos
    private String gerarNomeArquivoUnico(MultipartFile imagem) {
        return UUID.randomUUID() + "_" + imagem.getOriginalFilename();
    }
}
