package com.Harsh.Productivity.Os.ai.embedding;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record KeywordSearchResult(
        String embeddingId,
        String content,
        double score,
        Map<String, Object> metadata
) {
    public KeywordSearchResult{
        metadata=metadata==null?Map.of(): Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }
}
