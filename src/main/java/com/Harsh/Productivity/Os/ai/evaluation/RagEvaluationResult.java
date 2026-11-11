package com.Harsh.Productivity.Os.ai.evaluation;

import java.util.List;

public record RagEvaluationResult(String mode, String caseId, List<String> retrievedChunkKeys,RetrievalMetrics metrics) {
    public RagEvaluationResult{
        retrievedChunkKeys=List.copyOf(retrievedChunkKeys);
    }
}
