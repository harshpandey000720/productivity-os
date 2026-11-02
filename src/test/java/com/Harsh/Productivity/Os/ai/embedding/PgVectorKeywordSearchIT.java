package com.Harsh.Productivity.Os.ai.embedding;
import com.Harsh.Productivity.Os.ai.chunking.DocumentChunk;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringBootTest
@Testcontainers
public class PgVectorKeywordSearchIT {
    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("pgvector/pgvector:pg16")
                    .withDatabaseName("productivity_os_test")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver");
        registry.add("spring.flyway.locations",
                () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }
    @Autowired
    private VectorDocumentService vectorDocumentService;
    @Autowired
    private KeywordDocumentService keywordDocumentService;
    @Test
    void keywordAndVectorSearchReturnIndependentListsAndRespectScope(){
        String tenantA=UUID.randomUUID().toString();
        String tenantB=UUID.randomUUID().toString();
        String documentA=UUID.randomUUID().toString();
        String firstText="The sprint backlog sprint backlog is reviewed during weekly planning.";
        String secondText="The team reviews a sprint backlog before planning each week.";
        List<DocumentChunk> chunks=List.of(
                new DocumentChunk(
                        0,firstText,0,firstText.length(),Map.of()
                ),
                new DocumentChunk(
                        1,secondText,0,secondText.length(),Map.of()
                )
        );
        assertEquals(2,vectorDocumentService.indexChunks(tenantA,documentA,chunks));
        List<KeywordSearchResult> keywordMatches =
                keywordDocumentService.search(
                        tenantA, documentA, "sprint backlog", 5
                );

        List<EmbeddingMatch<TextSegment>> vectorMatches =
                vectorDocumentService.search(
                        tenantA,
                        documentA,
                        "What work is reviewed during weekly planning?",
                        5
                );
        assertEquals(2, keywordMatches.size());
        assertTrue(keywordMatches.get(0).score() >= keywordMatches.get(1).score());
        assertTrue(keywordMatches.get(0).content().contains("sprint backlog"));
        assertFalse(vectorMatches.isEmpty());

        assertTrue(
                keywordDocumentService.search(
                        tenantB, documentA, "sprint backlog", 5
                ).isEmpty()
        );

        assertTrue(
                keywordDocumentService.search(
                        tenantA,
                        UUID.randomUUID().toString(),
                        "sprint backlog",
                        5
                ).isEmpty()
        );
    }
}
