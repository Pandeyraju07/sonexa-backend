package com.sonexa.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class MediaStorageService {

    private final Path root;

    @Value("${sonexa.media.public-base-url:}")
    private String publicBaseUrl;

    public MediaStorageService(@Value("${sonexa.media.upload-dir:uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root.resolve("audio"));
        Files.createDirectories(root.resolve("covers"));
    }

    public Path getRoot() {
        return root;
    }

    public String storeAudio(MultipartFile file) throws IOException {
        return store(file, "audio");
    }

    public String storeCover(MultipartFile file) throws IOException {
        return store(file, "covers");
    }

    private String store(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.bin";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot);
        }
        String filename = UUID.randomUUID() + ext.toLowerCase();
        Path target = root.resolve(folder).resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return publicUrl("/media/" + folder + "/" + filename);
    }

    public String publicUrl(String path) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
            return base + path;
        }
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString();
        } catch (Exception e) {
            return "http://localhost:8080" + path;
        }
    }
}
