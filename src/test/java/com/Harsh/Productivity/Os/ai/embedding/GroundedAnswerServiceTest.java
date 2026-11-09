package com.Harsh.Productivity.Os.ai.embedding;

import com.Harsh.Productivity.Os.ai.gateway.AiGateway;
import com.Harsh.Productivity.Os.ai.service.GroundedAnswerAiService;
import com.Harsh.Productivity.Os.ai.service.GroundedAnswerDraft;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroundedAnswerServiceTest {
    @Test
    void returnsOnlyCitationIdsFromScopedRetrievedEvidence() {
        HybridDocumentSearchService search = mock(HybridDocumentSearchService.class);
        CandidateReranker reranker = mock(CandidateReranker.class);
        AiGateway gateway = mock(AiGateway.class);

        GroundedAnswerService service =
                new GroundedAnswerService(search, reranker, gateway);

        HybridSearchCandidate candidate = new HybridSearchCandidate(
                "chunk-1",
                "Employees can carry forward up to five unused leave days.",
                0.03,
                1,
                1,
                0.8,
                0.7,
                Map.of(
                        "tenantId", "tenant-1",
                        "documentId", "doc-1",
                        "chunkIndex", 2,
                        "startOffset", 100,
                        "endOffset", 160,
                        "source", "leave-policy.txt"
                )
        );

        when(search.search("tenant-1", "doc-1", "leave carryover", 12))
                .thenReturn(List.of(candidate));
        when(reranker.rerank("leave carryover", List.of(candidate), 4))
                .thenReturn(List.of(new RerankedCandidate(candidate, 0.91, 1)));

        GroundedAnswerDraft draft = new GroundedAnswerDraft(
                "Up to five unused days can be carried forward.",
                List.of("chunk-1", "made-up-id"),
                false
        );

        when(gateway.<GroundedAnswerAiService, GroundedAnswerDraft>call(
                eq(GroundedAnswerAiService.class),
                org.mockito.ArgumentMatchers
                        .<Function<GroundedAnswerAiService, GroundedAnswerDraft>>any()
        )).thenReturn(draft);

        GroundedAnswerResponse response =
                service.answer("tenant-1", "doc-1", "leave carryover");

        assertFalse(response.insufficientEvidence());
        assertEquals("LOCAL_ONNX_CROSS_ENCODER", response.rankingMode());
        assertEquals(1, response.citations().size());
        assertEquals("chunk-1", response.citations().getFirst().citationId());
        assertEquals("leave-policy.txt", response.citations().getFirst().source());
    }
}