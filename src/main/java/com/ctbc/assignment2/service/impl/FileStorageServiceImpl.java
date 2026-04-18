package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.exception.InvalidFileException;
import com.ctbc.assignment2.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) throws InvalidFileException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("No file uploaded");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidFileException("File exceeds 2MB limit");
        }
        String contentType = file.getContentType();
        String ext;
        if ("image/jpeg".equals(contentType)) {
            ext = "jpg";
        } else if ("image/png".equals(contentType)) {
            ext = "png";
        } else {
            throw new InvalidFileException("Only JPG and PNG images are allowed");
        }

        String filename = UUID.randomUUID().toString() + "." + ext;
        Path target = Paths.get(uploadDir).resolve(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new InvalidFileException("Failed to store file");
        }

        String normalizedDir = uploadDir.replace("\\", "/");
        if (normalizedDir.startsWith("/")) {
            normalizedDir = normalizedDir.substring(1);
        }
        return "/" + normalizedDir + "/" + filename;
    }
}
