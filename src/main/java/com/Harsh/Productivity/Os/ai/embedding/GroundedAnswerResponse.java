package com.Harsh.Productivity.Os.ai.embedding;

import java.util.List;

public record GroundedAnswerResponse(
        String answer,
        boolean insufficientEvidence,
        String rankingMode,
        List<AnswerCitation> citations
){
    public GroundedAnswerResponse{
        citations=citations==null?List.of():List.copyOf(citations);
    }
}
