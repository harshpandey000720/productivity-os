package com.Harsh.Productivity.Os.ai.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;
    private final int expectedDimension;
    private final String documentPrefix;
    private final String queryPrefix;
    public EmbeddingService(EmbeddingModel embeddingModel, @Value("${ai.embedding.expected-dimension:768}") int expectedDimension,@Value("${ai.embedding.document-prefix:}") String documentPrefix, @Value("${ai.embedding.query-prefix:}") String queryPrefix) {
        if(expectedDimension < 0){
            throw new IllegalArgumentException("Expected dimension cannot be negative");
        }
        this.embeddingModel = embeddingModel;
        this.expectedDimension = expectedDimension;
        this.documentPrefix = documentPrefix;
        this.queryPrefix = queryPrefix;
    }
    public List<Float>embedDocument(String text){
        return embed(documentPrefix,text);
    }
    public List<Float>embedQuery(String text){
        return embed(queryPrefix,text);
    }
    private List<Float> embed(String prefix, String text){
        if(text==null || text.isBlank()){
            throw new IllegalArgumentException("Text must not be blank");
        }
        String trimmedText = text.trim();
        String normalizedPrefix=prefix==null?"":prefix.trim();
        String input=normalizedPrefix.isEmpty()
                ?trimmedText
                :normalizedPrefix+" "+trimmedText;
        var response=embeddingModel.embed(input);
        if(response==null||response.content()==null){
            throw new IllegalStateException("Embedding model returned no result");
        }
        List<Float> vector=response.content().vectorAsList();
        if(vector==null||vector.isEmpty()){
            throw new IllegalStateException("Embedding model returned an empty vector");
        }
        if (expectedDimension > 0 && vector.size() != expectedDimension) {
            throw new IllegalStateException(
                    "Expected embedding dimension " + expectedDimension
                            + " but received " + vector.size()
            );
        }
        return List.copyOf(vector);
    }
}
