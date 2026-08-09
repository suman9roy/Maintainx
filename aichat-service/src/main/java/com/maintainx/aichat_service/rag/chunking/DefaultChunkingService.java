package com.maintainx.aichat_service.rag.chunking;



import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultChunkingService implements ChunkingService {

    private static final int CHUNK_SIZE = 1000;

    private static final int CHUNK_OVERLAP = 200;

    @Override
    public List<DocumentChunkRequest> chunk(String text) {

        List<DocumentChunkRequest> chunks = new ArrayList<>();
        log.info("Starting chunking process for text of length: {}", text.length());
        int index = 0;
        int chunkNumber = 0;

        while (index < text.length()) {

            int end = Math.min(
                    index + CHUNK_SIZE,
                    text.length()
            );

            String chunk = text.substring(index, end);

            chunks.add(
                    new DocumentChunkRequest(
                            chunkNumber++,
                            chunk,
                            estimateTokens(chunk),
                            null,
                            null
                    )
            );
            log.info("Created chunk {}: {} characters", chunkNumber - 1, chunk.length());

            index += (CHUNK_SIZE - CHUNK_OVERLAP);
        }
        log.info("Finished chunking process. Total chunks created: {}", chunks);
        return chunks;
    }

    private int estimateTokens(String text) {

        return text.split("\\s+").length;
    }

}