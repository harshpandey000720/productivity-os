package com.Harsh.Productivity.Os.ai.embedding;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record HybridSearchCandidate(
        String embeddingId,
        String content,
        double rrfScore,
        Integer vectorRank,
        Integer keywordRank,
        Double vectorScore,
        Double keywordScore,
        Map<String,Object> metadata
){
    public HybridSearchCandidate{
        metadata=metadata==null?Map.of(): Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
    }
}
