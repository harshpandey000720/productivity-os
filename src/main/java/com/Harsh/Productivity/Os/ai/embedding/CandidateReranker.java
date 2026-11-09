package com.Harsh.Productivity.Os.ai.embedding;

import java.util.List;

public interface CandidateReranker {
    List<RerankedCandidate> rerank(String query,List<HybridSearchCandidate> candidates,int maxResult);
}
