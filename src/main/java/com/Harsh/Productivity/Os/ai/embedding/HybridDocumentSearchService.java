package com.Harsh.Productivity.Os.ai.embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class HybridDocumentSearchService {
    private final VectorDocumentService vectorDocumentService;
    private final KeywordDocumentService keywordDocumentService;
    private final RrfFusionService rrfFusionService;
    public HybridDocumentSearchService(
            VectorDocumentService vectorDocumentService,
            KeywordDocumentService keywordDocumentService,
            RrfFusionService rrfFusionService
    ) {
        this.vectorDocumentService = vectorDocumentService;
        this.keywordDocumentService = keywordDocumentService;
        this.rrfFusionService = rrfFusionService;
    }
    public List<HybridSearchCandidate> search(
            String tenantId,
            String documentId,
            String query,
            int topK
    ) {
        List<EmbeddingMatch<TextSegment>> vectorMatches =
                vectorDocumentService.search(
                        tenantId, documentId, query, topK
                );

        List<KeywordSearchResult> keywordMatches =
                keywordDocumentService.search(
                        tenantId, documentId, query, topK
                );

        return rrfFusionService.fuse(
                vectorMatches,
                keywordMatches,
                topK
        );
    }
}
