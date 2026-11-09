package com.Harsh.Productivity.Os.ai.service;
import java.util.List;
public record GroundedAnswerDraft(String answer,List<String> citationIds,boolean insufficientEvidence){
    public GroundedAnswerDraft{
        citationIds = citationIds==null?List.of():List.copyOf(citationIds);
    }
}
