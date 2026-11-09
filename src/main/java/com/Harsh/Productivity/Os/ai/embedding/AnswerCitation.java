package com.Harsh.Productivity.Os.ai.embedding;
public record AnswerCitation(
        String citationId,
        String documentId,
        Integer chunkIndex,
        String source,
        Integer startOffset,
        Integer endOffset,
        String excerpt
) {
}
