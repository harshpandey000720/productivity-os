package com.Harsh.Productivity.Os.ai.evaluation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RagMetricCalculator {
    private RagMetricCalculator() {
    }

    public static RetrievalMetrics calculate(
            List<String> rankedChunkKeys,
            Set<String> relevantChunkKeys,
            int k,
            long latencyMillis
    ) {
        if (k < 1) {
            throw new IllegalArgumentException("k must be at least 1");
        }
        if (relevantChunkKeys==null || relevantChunkKeys.isEmpty()) {
            throw new IllegalArgumentException("relevantChunkKeys must not be empty");
        }
        List<String> topK=new LinkedHashSet<>(rankedChunkKeys==null?List.of():rankedChunkKeys).stream().limit(k).toList();
        long hits=topK.stream()
                .filter(relevantChunkKeys::contains)
                .count();
        double precision=hits/(double) k;
        double recall=hits/(double) relevantChunkKeys.size();
        double reciprocalRank=0.0;
        for (int i = 0; i < topK.size(); i++) {
            if (relevantChunkKeys.contains(topK.get(i))) {
                reciprocalRank=1.0/(i+1);
                break;
            }
        }
        return new RetrievalMetrics(
                precision,
                recall,
                reciprocalRank,
                Math.max(0,latencyMillis)
        );
    }
}


