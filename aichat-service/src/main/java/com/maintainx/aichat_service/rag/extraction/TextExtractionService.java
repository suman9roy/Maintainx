package com.maintainx.aichat_service.rag.extraction;

import java.io.IOException;
import java.nio.file.Path;

public interface TextExtractionService {

    /**
     * Extracts plain text from a stored document.
     *
     * @param documentPath Absolute path of the stored document.
     * @return Extracted text.
     */
    String extract(Path documentPath) throws IOException;

}