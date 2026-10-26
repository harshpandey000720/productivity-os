package com.Harsh.Productivity.Os.ai.chunking;

import java.util.Map;

/*
Offsets are half open Java String positions into the normalized text
passed to the chunker:startOffset is included endOffset is excluded
 */
public record DocumentChunk(int chunkIndex, String content, int startOffset, int endOffset, Map<String ,String>metadata) {
    public DocumentChunk{
        if (chunkIndex<0) throw  new IllegalArgumentException("chunkIndex cannot be negative");
        if(content==null||content.isBlank()) throw  new IllegalArgumentException("content must not be blank");
        if(startOffset<0||endOffset<startOffset) throw  new IllegalArgumentException("Invalid chunk offsers");
        if (endOffset-startOffset!=content.length()) throw  new IllegalArgumentException("Offset range must match content length");
        metadata=metadata==null?Map.of():Map.copyOf(metadata);
    }
}
