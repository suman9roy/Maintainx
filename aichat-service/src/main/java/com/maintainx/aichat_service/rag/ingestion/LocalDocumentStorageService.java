package com.maintainx.aichat_service.rag.ingestion;



import com.maintainx.aichat_service.config.DocumentStorageProperties;
import com.maintainx.aichat_service.security.GatewayContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalDocumentStorageService implements DocumentStorageService {

    private final DocumentStorageProperties storageProperties;

    @Override
    public DocumentMetadata store(
            GatewayContext context,
            MultipartFile file
    ) throws IOException {

        validate(file);

        String originalFileName =
                StringUtils.cleanPath(file.getOriginalFilename());
        log.info("Storing document: {}", originalFileName);

        String extension = getExtension(originalFileName);
        log.info("Document extension: {}", extension);

        String storedFileName =
                UUID.randomUUID() + "-" + originalFileName;
        log.info("Generated stored file name: {}", storedFileName);
        Path apartmentDirectory = Path.of(
                storageProperties.location(),
                context.apartmentId(),
                String.valueOf(Year.now().getValue())
        );
        log.info("Apartment directory: {}", apartmentDirectory);

        Files.createDirectories(apartmentDirectory);
        log.info("Created apartment directory: {}", apartmentDirectory);
        Path destination = apartmentDirectory.resolve(storedFileName);

        Files.copy(
                file.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        return new DocumentMetadata(
                context.apartmentId(),
                originalFileName,
                storedFileName,
                file.getContentType(),
                file.getSize(),
                destination.toString(),
                Instant.now()
        );
    }

    @Override
    public void delete(String fileName) {
        Path filePath = Path.of(
                storageProperties.location(),
                fileName
        );

        try {
            Files.deleteIfExists(filePath);
            //need to delete the metadata from the database as well
          //forward the delete event to the DocumentServiceImpl to handle the metadata deletion


        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete file: " + fileName,
                    e
            );
        }
    }

    private void validate(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        if (file.getSize() > storageProperties.maxFileSize()) {
            throw new IllegalArgumentException("File size exceeds the configured limit.");
        }

        String extension = getExtension(file.getOriginalFilename());

        List<String> allowed =
                storageProperties.allowedExtensions();

        if (!allowed.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + extension
            );
        }
    }

    private String getExtension(String filename) {

        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}