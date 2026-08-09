

package com.maintainx.aichat_service.rag.chunking;

public record DocumentChunkRequest(

        Integer chunkIndex,

        String content,

        Integer tokenCount,

        Integer pageNumber,

        String section

) {
}