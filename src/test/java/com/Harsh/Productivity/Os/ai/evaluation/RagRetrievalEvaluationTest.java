package com.Harsh.Productivity.Os.ai.evaluation;

import com.Harsh.Productivity.Os.ai.chunking.DocumentChunk;
import com.Harsh.Productivity.Os.ai.embedding.CandidateReranker;
import com.Harsh.Productivity.Os.ai.embedding.HybridDocumentSearchService;
import com.Harsh.Productivity.Os.ai.embedding.HybridSearchCandidate;
import com.Harsh.Productivity.Os.ai.embedding.KeywordDocumentService;
import com.Harsh.Productivity.Os.ai.embedding.KeywordSearchResult;
import com.Harsh.Productivity.Os.ai.embedding.RerankedCandidate;
import com.Harsh.Productivity.Os.ai.embedding.VectorDocumentService;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@EnabledIfEnvironmentVariable(
        named = "OLLAMA_EMBEDDING_TESTS",
        matches = "true"
)
public class RagRetrievalEvaluationTest {
    private static final int K = 3;
    private static final int RERANK_CANDIDATES = 12;
    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("pgvector/pgvector:pg16")
                    .withDatabaseName("rag_evaluation_test")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );
        registry.add(
                "spring.flyway.locations",
                () -> "classpath:db/migration"
        );
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private VectorDocumentService vectorDocumentService;

    @Autowired
    private KeywordDocumentService keywordDocumentService;

    @Autowired
    private HybridDocumentSearchService hybridDocumentSearchService;

    @Autowired
    private CandidateReranker candidateReranker;

    private final RagEvaluationRunner runner = new RagEvaluationRunner();

    @Test
    void evaluatesSameCasesAcrossKeywordVectorHybridAndRerankedSearch() {
        String tenantId = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        int indexed = vectorDocumentService.indexChunks(tenantId, documentId, evaluationChunks());
        assertEquals(4, indexed, "All evaluation chunks should be indexed");
        List<RagEvaluationCase> cases = List.of(
                new RagEvaluationCase(
                        "leave-carryover",
                        "How many unused leave days can be carried forward?",
                        Set.of(documentId + ":0")
                ),
                new RagEvaluationCase(
                        "leave-request",
                        "How early must I ask my manager for time away?",
                        Set.of(documentId + ":1")
                ),
                new RagEvaluationCase(
                        "laptop-security",
                        "What protects laptop data if the device is lost?",
                        Set.of(documentId + ":2")
                )
        );
        List<RagEvaluationResult> results = new ArrayList<>();
        for (RagEvaluationCase evaluationCase : cases) {
            results.addAll(evaluationModes(tenantId, documentId, evaluationCase));
        }
        assertEquals(cases.size() * 4, results.size());
        results.forEach(this::printResult);
        printMacroAverages(results);
    }

    private List<RagEvaluationResult> evaluationModes(String tenantId, String documentId, RagEvaluationCase evaluationCase) {
        List<RagEvaluationResult> results = new ArrayList<>();
        results.add(runner.evaluate(
                "keyword",
                evaluationCase,
                K,
                () -> keywordDocumentService.search(
                        tenantId,
                        documentId,
                        evaluationCase.question(),
                        K
                ),
                result -> key(result.metadata())
        ));
        results.add(runner.evaluate(
                "vector",
                evaluationCase,
                K,
                () -> vectorDocumentService.search(
                        tenantId,
                        documentId,
                        evaluationCase.question(),
                        K
                ),
                match -> key(
                        match.embedded().metadata().getString("documentId"),
                        match.embedded().metadata().getInteger("chunkIndex")
                )
        ));
        results.add(runner.evaluate(
                "hybrid-rrf",
                evaluationCase,
                K,
                () -> hybridDocumentSearchService.search(
                        tenantId,
                        documentId,
                        evaluationCase.question(),
                        K
                ),
                candidate -> key(candidate.metadata())
        ));
        results.add(runner.evaluate(
                "hybrid-reranked",
                evaluationCase,
                K,
                () -> rerank(
                        tenantId,
                        documentId,
                        evaluationCase.question()
                ),
                candidate -> key(candidate.metadata())
        ));
        return results;
    }

    private List<HybridSearchCandidate> rerank(String tenantId, String documentId, String question) {
        List<HybridSearchCandidate> candidates = hybridDocumentSearchService.search(tenantId, documentId, question, RERANK_CANDIDATES);
        if (candidates.isEmpty()) {
            return List.of();
        }
        return candidateReranker.rerank(question, candidates, K)
                .stream()
                .map(RerankedCandidate::candidate)
                .toList();
    }

    private static List<DocumentChunk> evaluationChunks() {
        List<String> texts = List.of(
                "Unused leave carryover: Employees may carry forward up to five "
                        + "unused leave days into the next calendar year.",
                "Leave requests must be submitted to the manager at least two "
                        + "weeks before the planned absence.",
                "Company laptops must use full disk encryption and automatically "
                        + "lock after five minutes of inactivity.",
                "Employees may work remotely up to two days per week with "
                        + "manager approval."
        );
        List<DocumentChunk> chunks = new ArrayList<>();
        int startOffset = 0;
        for (int i = 0; i < texts.size(); i++) {
            String content = texts.get(i);
            int endOffset = startOffset + content.length();
            chunks.add(new DocumentChunk(i, content, startOffset, endOffset, Map.of("source", "day-7-evaluation.txt")));
            startOffset = endOffset + 1;
        }
        return List.copyOf(chunks);
    }

    private static String key(Map<String, Object> metadata) {
        return key(
                metadata.get("documentId"),
                metadata.get("chunkIndex")
        );
    }

    private static String key(Object documentId, Object chunkIndex) {
        if (documentId == null || chunkIndex == null) {
            throw new IllegalStateException(
                    "Retrieved chunk is missing documentId or chunkIndex metadata"
            );
        }
        return documentId + ":" + chunkIndex;
    }

    private void printResult(RagEvaluationResult result) {
        RetrievalMetrics metrics = result.metrics();
        System.out.printf(
                "%s | %s | P@%d=%.3f Recall@%d=%.3f MRR@%d=%.3f "
                        + "latency=%dms | retrieved=%s%n",
                result.mode(),
                result.caseId(),
                K, metrics.precisionAtK(),
                K, metrics.recallAtK(),
                K, metrics.mrrAtK(),
                metrics.latencyMillis(),
                result.retrievedChunkKeys()
        );
    }

    private void printMacroAverages(List<RagEvaluationResult> results) {
        Map<String, List<RagEvaluationResult>> byMode = results.stream()
                .collect(Collectors.groupingBy(
                        RagEvaluationResult::mode,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        System.out.println("--- Macro averages across evaluation cases ---");

        byMode.forEach((mode, modeResults) -> {
            double precision = modeResults.stream()
                    .mapToDouble(r -> r.metrics().precisionAtK())
                    .average()
                    .orElse(0.0);

            double recall = modeResults.stream()
                    .mapToDouble(r -> r.metrics().recallAtK())
                    .average()
                    .orElse(0.0);

            double mrr = modeResults.stream()
                    .mapToDouble(r -> r.metrics().mrrAtK())
                    .average()
                    .orElse(0.0);

            double latency = modeResults.stream()
                    .mapToLong(r -> r.metrics().latencyMillis())
                    .average()
                    .orElse(0.0);

            System.out.printf(
                    "%s | avg P@%d=%.3f avg Recall@%d=%.3f "
                            + "avg MRR@%d=%.3f avg latency=%.1fms%n",
                    mode,
                    K, precision,
                    K, recall,
                    K, mrr,
                    latency
            );
        });
    }
}
