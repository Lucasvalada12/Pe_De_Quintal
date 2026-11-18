package br.edu.aula.pedequintal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Guarda a (única) foto do anúncio em uma pasta local do servidor.
 * Simplificação do MVP: nada de nuvem/Storage.
 */
@Service
public class FotoService {

    private static final Map<String, String> EXTENSOES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final Path pasta;

    public FotoService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.pasta = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /** Salva a foto e devolve a URL pública (ex.: /uploads/abc.jpg), ou null se não houver foto. */
    public String salvar(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            return null;
        }
        String extensao = EXTENSOES.get(foto.getContentType());
        if (extensao == null) {
            throw new RegraNegocioException("Envie uma imagem JPG, PNG, WEBP ou GIF.");
        }
        String nomeArquivo = UUID.randomUUID() + extensao;   // nome aleatório: evita colisão e path traversal
        try (InputStream in = foto.getInputStream()) {
            Files.createDirectories(pasta);
            Files.copy(in, pasta.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível salvar a foto. Tente novamente.");
        }
        return "/uploads/" + nomeArquivo;
    }

    /** Remove o arquivo da foto (usado ao excluir um anúncio). Falhas são ignoradas. */
    public void remover(String fotoUrl) {
        if (fotoUrl == null || !fotoUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            Path arquivo = pasta.resolve(fotoUrl.substring("/uploads/".length())).normalize();
            if (arquivo.startsWith(pasta)) {
                Files.deleteIfExists(arquivo);
            }
        } catch (IOException ignorado) {
            // a foto órfã não impede a exclusão do anúncio
        }
    }

    public Path getPasta() {
        return pasta;
    }
}
