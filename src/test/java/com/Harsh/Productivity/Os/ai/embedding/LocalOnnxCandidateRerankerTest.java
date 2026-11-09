package com.Harsh.Productivity.Os.ai.embedding;

import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalOnnxCandidateRerankerTest {
    @Test
    void reordersFusedCandidatesByModelScoresAndKeepsOriginalCandidates() {
        ScoringModel scoringModel = mock(ScoringModel.class);
        LocalOnnxCandidateReranker reranker =
                new LocalOnnxCandidateReranker(scoringModel);

        HybridSearchCandidate first = candidate("chunk-a", 0.04);
        HybridSearchCandidate second = candidate("chunk-b", 0.03);

        when(scoringModel.scoreAll(anyList(), eq("leave carryover")))
                .thenReturn(Response.from(List.of(0.15, 0.92)));

        List<RerankedCandidate> results = reranker.rerank(
                "leave carryover",
                List.of(first, second),
                2
        );

        assertEquals(2, results.size());
        assertEquals("chunk-b", results.get(0).candidate().embeddingId());
        assertEquals(0.92, results.get(0).rerankScore());
        assertEquals(1, results.get(0).rerankRank());
        assertEquals("chunk-a", results.get(1).candidate().embeddingId());
        assertEquals(0.15, results.get(1).rerankScore());

        verify(scoringModel).scoreAll(anyList(), eq("leave carryover"));
    }

    private static HybridSearchCandidate candidate(String id, double rrfScore) {
        return new HybridSearchCandidate(
                id,
                "Employees can carry forward unused leave.",
                rrfScore,
                1,
                1,
                0.8,
                0.7,
                Map.of("tenantId", "tenant-1", "documentId", "doc-1")
        );
    }
}