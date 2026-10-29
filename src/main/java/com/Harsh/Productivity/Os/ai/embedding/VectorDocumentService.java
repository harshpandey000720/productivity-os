package com.Harsh.Productivity.Os.ai.embedding;
import com.Harsh.Productivity.Os.ai.chunking.DocumentChunk;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
@Service
public class VectorDocumentService {
    private final EmbeddingService embeddingService;
    private final EmbeddingStore<TextSegment> embeddingStore;
    public VectorDocumentService(
            EmbeddingService embeddingService,
            @Qualifier("documentEmbeddingStore")
            EmbeddingStore<TextSegment> embeddingStore
    ) {
        this.embeddingService = embeddingService;
        this.embeddingStore = embeddingStore;
    }
    public int indexChunks(
            String tenantId,
            String documentId,
            List<DocumentChunk> chunks
    ) {
        requireText(tenantId, "tenantId");
        requireText(documentId, "documentId");
        if (chunks == null || chunks.isEmpty()) {
            return 0;
        }
        List<TextSegment> segments = chunks.stream()
                .filter(chunk -> chunk.content() != null && !chunk.content().isBlank())
                .map(chunk -> {
                    Map<String, Object> metadata = new HashMap<>(chunk.metadata());
                    // Scope comes from trusted service arguments, overriding chunk metadata.
                    metadata.put("tenantId", tenantId);
                    metadata.put("documentId", documentId);
                    metadata.put("chunkIndex", chunk.chunkIndex());
                    metadata.put("startOffset", chunk.startOffset());
                    metadata.put("endOffset", chunk.endOffset());
                    return TextSegment.from(
                            chunk.content(),
                            Metadata.from(metadata)
                    );
                })
                .toList();
        if (segments.isEmpty()) {
            return 0;
        }
        List<Embedding> embeddings = segments.stream()
                .map(segment -> toEmbedding(
                        embeddingService.embedDocument(segment.text())
                ))
                .toList();
        embeddingStore.addAll(embeddings, segments);
        return segments.size();
    }
    public List<EmbeddingMatch<TextSegment>> search(String tenantId,String documentId,String question,int topK){
        requireText(tenantId, "tenantId");
        requireText(documentId, "documentId");
        requireText(question, "question");
        if (topK < 1 || topK > 20) {
            throw new IllegalArgumentException("topK must be between 1 and 20");
        }
        Embedding queryEmbedding = toEmbedding(
                embeddingService.embedQuery(question)
        );
        var scopeFilter = metadataKey("tenantId").isEqualTo(tenantId)
                .and(metadataKey("documentId").isEqualTo(documentId));
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .filter(scopeFilter)
                .build();
        return embeddingStore.search(request).matches();
    }
    private static Embedding toEmbedding(List<Float> vector) {
        float[] values = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            values[i] = vector.get(i);
        }
        return Embedding.from(values);
    }
    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}