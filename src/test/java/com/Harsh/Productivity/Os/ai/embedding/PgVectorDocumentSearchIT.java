package com.Harsh.Productivity.Os.ai.embedding;

import com.Harsh.Productivity.Os.ai.chunking.DocumentChunk;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.segment.TextSegment;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class PgVectorDocumentSearchIT {
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
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private VectorDocumentService vectorDocumentService;

    @Test
    void storesAndSearchesChunkOnlyWithinRequestedTenantAndDocument() {
        String tenantA = UUID.randomUUID().toString();
        String tenantB = UUID.randomUUID().toString();
        String documentA = UUID.randomUUID().toString();
        String text = "weekly planning reviews the sprint backlog and prioritizes tasks.";
        DocumentChunk chunk = new DocumentChunk(0, text, 0, text.length(), Map.of());
        int indexed = vectorDocumentService.indexChunks(tenantA, documentA, List.of(chunk));
        assertTrue(indexed > 0);
        List<EmbeddingMatch<TextSegment>> matchingScope =
                vectorDocumentService.search(
                        tenantA,
                        documentA,
                        "How are tasks prioritized during weekly planning?",
                        3
                );
        assertFalse(matchingScope.isEmpty());
        assertTrue(matchingScope.get(0).embedded().text().contains("sprint backlog"));
        List<EmbeddingMatch<TextSegment>> wrongTenant =
                vectorDocumentService.search(
                        tenantB,
                        documentA,
                        "How are tasks prioritized during weekly planning?",
                        3
                );
        assertTrue(wrongTenant.isEmpty());
        List<EmbeddingMatch<TextSegment>> wrongDocument =
                vectorDocumentService.search(
                        tenantA,
                        UUID.randomUUID().toString(),
                        "How are tasks prioritized during weekly planning?",
                        3
                );
        assertTrue(wrongDocument.isEmpty());
    }
}

