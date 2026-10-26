package com.Harsh.Productivity.Os.ai.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfiguration {
    @Bean
    @ConditionalOnMissingBean(EmbeddingModel.class)
    public EmbeddingModel documentEmbeddingModel(@Value("${ai.embedding.base-url}") String baseUrl,
                                                 @Value("${ai.embedding.model}") String modelName) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }
}
