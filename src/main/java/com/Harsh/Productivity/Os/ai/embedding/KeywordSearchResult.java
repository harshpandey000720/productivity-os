package com.Harsh.Productivity.Os.ai.embedding;

public record KeywordSearchResult(
        String embeddingId,
        String content,
        double score,
        Integer chunkIndex,
        Integer startOffset,
        Integer endOffset
) {
}
