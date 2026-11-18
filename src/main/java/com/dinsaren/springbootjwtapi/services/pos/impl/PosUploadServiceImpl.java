package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.services.pos.PosUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class PosUploadServiceImpl implements PosUploadService {

    private final Path uploadPath = Paths.get("public", "uploads").toAbsolutePath();

    public PosUploadServiceImpl() {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            log.error("Could not initialize upload directory: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }

        try {
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            long timestamp = System.currentTimeMillis();
            long rand = Math.round(new Random().nextDouble() * 1e9);
            String filename = timestamp + "-" + rand + "-" + cleanName;

            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath);

            String fileUrl = "/uploads/" + filename;
            return Map.of("url", fileUrl);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file: " + e.getMessage());
        }
    }
}
