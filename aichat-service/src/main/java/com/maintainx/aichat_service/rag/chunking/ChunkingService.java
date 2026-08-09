package com.maintainx.aichat_service.rag.chunking;



import java.util.List;

public interface ChunkingService {

    List<DocumentChunkRequest> chunk(String text);

}