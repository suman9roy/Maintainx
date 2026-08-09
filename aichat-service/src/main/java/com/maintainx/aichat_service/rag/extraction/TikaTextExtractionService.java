package com.maintainx.aichat_service.rag.extraction;



import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class TikaTextExtractionService implements TextExtractionService {

    private final Tika tika = new Tika();

    @Override
    public String extract(Path documentPath) throws IOException {

        log.info("Extracting text from document: {}", documentPath);

        try (var inputStream = Files.newInputStream(documentPath)) {

            return tika.parseToString(inputStream);

        } catch (Exception ex) {

            throw new IOException(
                    "Failed to extract text from document: " + documentPath,
                    ex
            );
        }
    }
}