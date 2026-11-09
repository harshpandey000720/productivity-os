package com.Harsh.Productivity.Os.ai.embedding;

public record RerankedCandidate(HybridSearchCandidate candidate,Double rerankScore,int rerankRank) {
    public RerankedCandidate{
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }
        if (rerankRank<1){
            throw new IllegalArgumentException("rerankRank must be at least 1");
        }
    }
}
