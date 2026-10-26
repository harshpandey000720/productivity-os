package com.Harsh.Productivity.Os.ai.config;
import com.Harsh.Productivity.Os.ai.embedding.EmbeddingService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringJUnitConfig(classes = {
        EmbeddingConfiguration.class,
        EmbeddingService.class
})
@TestPropertySource(properties = {
        "ai.embedding.base-url=http://localhost:11434",
        "ai.embedding.model=nomic-embed-text",
        "ai.embedding.expected-dimension=768",
        "ai.embedding.document-prefix=search_document:",
        "ai.embedding.query-prefix=search_query:"
})
@EnabledIfEnvironmentVariable(
        named = "OLLAMA_EMBEDDING_TESTS",
        matches = "true"
)
public class EmbeddingModelSmokeTest {
    @Autowired
    private EmbeddingModel embeddingModel;
    @Test
    void shouldCreateAnEmbeddingAndReportItsDimension(){
        var embedding=embeddingModel
                .embed("Nexus embedding smoke test")
                .content();
        assertTrue(embedding.dimension() > 0);
        System.out.println("Embedding dimension: " + embedding.dimension());
    }
    @Autowired
    private EmbeddingService embeddingService;
    @Test
    void documentAndQueryInputsProduceExceptedDimension(){
        var documentVector=embeddingService.embedDocument("Productivity OS project notes.");
        var queryVector=embeddingService.embedQuery("Where are projects notes stored?");
        assertEquals(768,documentVector.size());
        assertEquals(768,queryVector.size());
        System.out.println("Verified document/query embedding dimension:"+documentVector.size());
    }
}