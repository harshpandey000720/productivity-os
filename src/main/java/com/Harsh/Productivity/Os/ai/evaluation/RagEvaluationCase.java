package com.Harsh.Productivity.Os.ai.evaluation;

import java.util.Set;

public record RagEvaluationCase(String caseId, String question, Set<String>relevantChunkKeys){
    public RagEvaluationCase{
        if(caseId == null||caseId.isBlank()){
            throw new IllegalArgumentException("caseId must not be blank");
        }
        if(question == null||question.isBlank()){
            throw new IllegalArgumentException("question must not be blank");
        }
        relevantChunkKeys = relevantChunkKeys==null?Set.of():Set.copyOf(relevantChunkKeys);
        if(relevantChunkKeys.isEmpty()){
            throw new IllegalArgumentException("At least one relevant chunk key is required");
        }
    }
}
