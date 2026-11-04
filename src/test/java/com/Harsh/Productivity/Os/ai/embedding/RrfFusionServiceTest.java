package com.Harsh.Productivity.Os.ai.embedding;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RrfFusionServiceTest {

    private final RrfFusionService fusionService =
            new RrfFusionService(60);

    @Test
    void mergesByIdDeduplicatesAndAddsReciprocalRankContributions() {
        List<EmbeddingMatch<TextSegment>> vectorResults = List.of(
                vectorMatch(
                        "shared",
                        "Shared chunk",
                        0.12,
                        Map.of("source", "notes.txt")
                ),
                vectorMatch(
                        "vector-only",
                        "Vector-only chunk",
                        0.99,
                        Map.of("source", "vector.txt")
                )
        );

        List<KeywordSearchResult> keywordResults = List.of(
                new KeywordSearchResult(
                        "shared",
                        "Shared chunk",
                        8.5,
                        Map.of("source", "notes.txt")
                ),
                new KeywordSearchResult(
                        "keyword-only",
                        "Keyword-only chunk",
                        0.0001,
                        Map.of("source", "keyword.txt")
                )
        );

        List<HybridSearchCandidate> results =
                fusionService.fuse(vectorResults, keywordResults, 10);

        assertEquals(3, results.size());

        HybridSearchCandidate shared = results.get(0);
        assertEquals("shared", shared.embeddingId());
        assertEquals(1, shared.vectorRank());
        assertEquals(1, shared.keywordRank());
        assertEquals(0.12, shared.vectorScore());
        assertEquals(8.5, shared.keywordScore());
        assertEquals(2.0 / 61.0, shared.rrfScore(), 1e-12);
        assertEquals("notes.txt", shared.metadata().get("source"));

        HybridSearchCandidate second = results.get(1);
        assertEquals(1.0 / 62.0, second.rrfScore(), 1e-12);
    }

    private static EmbeddingMatch<TextSegment> vectorMatch(
            String id,
            String content,
            double score,
            Map<String, Object> metadata
    ) {
        TextSegment segment = TextSegment.from(
                content,
                Metadata.from(metadata)
        );

        return new EmbeddingMatch<>(score, id, null, segment);
    }
}