package br.com.gabwsv.secure_todo.service;

import org.apache.tika.Tika;
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

@Service
public class FileStorageService {

    // Whitelist dos Magic Bytes e Extensão
    private static final Map<String, String> ALLOWED_FILES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "pdf", "application/pdf"
    );

    private final Path fileStorageLocation;

    public FileStorageService(){
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try{
            // Criar a pasta automaticamente quando subir a aplicação.
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex){
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    public String storedFile(MultipartFile file){
        String originalFileName = file.getOriginalFilename();

        //Evitar path traversal ../../
        if(originalFileName == null || originalFileName.contains("..")){
            throw new SecurityException("Nome de arquivo inválido!");
        }

        String fileExtension = getFileExtension(originalFileName);
        if(!ALLOWED_FILES.containsKey(fileExtension)){
            throw new SecurityException("Extensão não permitida: ."+fileExtension);
        }
        try(InputStream inputStream = file.getInputStream()){
            Tika tika = new Tika();
            // Ler o "Magic Bytes" do arquivo.
            String detectedMimeType = tika.detect(inputStream);

            System.out.println("Arquivo: "+originalFileName+ " | Ext: " + fileExtension + " | Mime: " + detectedMimeType);

            String expectedMimeType = ALLOWED_FILES.get(fileExtension);

            if(!expectedMimeType.equals(detectedMimeType)) {
                // Se a extensão for diferente do Mime Bytes, alerta claro de tentativa de bypassar os filtros
                throw new SecurityException("Conteúdo do arquivo não condiz com a extensão. (Spoofing detectado)");
            }

            String fileName = UUID.randomUUID().toString() + "-"+originalFileName;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex){
            throw new RuntimeException("Falha ao armazenar arquivo "+ originalFileName, ex);
        }
    }

    private String getFileExtension(String fileName) {
        if(fileName == null || fileName.lastIndexOf(".") == -1){
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

}
