package com.Harsh.Productivity.Os.ai.embedding;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class RrfOnlyCandidateReranker implements CandidateReranker{
    @Override
    public List<RerankedCandidate> rerank(String query,List<HybridSearchCandidate> candidates,int maxResults) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (maxResults < 1 || maxResults > 20) {
            throw new IllegalArgumentException("maxResults must be between 1 and 20");
        }
        List<HybridSearchCandidate> sorted=candidates.stream()
                .sorted(
                        Comparator.comparingDouble(
                                HybridSearchCandidate::rrfScore
                        ).reversed()
                                .thenComparing(HybridSearchCandidate::embeddingId)
                )
                .limit(maxResults)
                .toList();
        List<RerankedCandidate> result=new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            result.add(new RerankedCandidate(sorted.get(i),null,i+1));
        }
        return List.copyOf(result);
    }
}
