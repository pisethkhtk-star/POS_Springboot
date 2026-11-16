package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/upload")
@Slf4j
@Tag(name = "POS Upload", description = "Upload product and ingredient images")
public class PosUploadController {

    private final Path uploadPath = Paths.get("public", "uploads").toAbsolutePath();

    public PosUploadController() {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            log.error("Could not initialize upload directory: {}", e.getMessage());
        }
    }

    @Operation(summary = "Upload image file")
    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
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
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", fileUrl));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file: " + e.getMessage());
        }
    }
}
