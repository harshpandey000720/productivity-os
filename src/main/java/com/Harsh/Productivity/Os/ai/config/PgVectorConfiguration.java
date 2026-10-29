package com.Harsh.Productivity.Os.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class PgVectorConfiguration {
    @Bean
    public EmbeddingStore<TextSegment> documentEmbeddingStore(
            DataSource dataSource,
            @Value("${ai.embedding.expected-dimension:768}") int embeddingDimension
    ) {
        if (embeddingDimension <= 0) {
            throw new IllegalArgumentException(
                    "ai.embedding.expected-dimension must be greater than zero"
            );
        }
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("document_embeddings")
                .dimension(embeddingDimension)
                .createTable(false)//Flyway owns creating table and schema
                .metadataStorageConfig(
                        DefaultMetadataStorageConfig.builder()
                                .storageMode(MetadataStorageMode.COMBINED_JSONB)
                                .columnDefinitions(List.of("metadata JSONB NULL"))
                                .build()
                )
                .build();
    }
}
