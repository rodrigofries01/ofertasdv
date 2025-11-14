package com.utfpr.ofertasdv.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileUploadService {

    private static final String UPLOADS_DIR = "uploads/";

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Files.createDirectories(Paths.get(UPLOADS_DIR));

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = Paths.get(UPLOADS_DIR).resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename;
    }

    public void deleteFile(String fotoUrl) throws IOException {
        if (fotoUrl == null || fotoUrl.isEmpty()) {
            return;
        }

        String filename = fotoUrl.substring(fotoUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(UPLOADS_DIR).resolve(filename);

        Files.deleteIfExists(filePath);
    }
}
