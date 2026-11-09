package com.Harsh.Productivity.Os.ai.config;
import com.Harsh.Productivity.Os.ai.embedding.CandidateReranker;
import com.Harsh.Productivity.Os.ai.embedding.LocalOnnxCandidateReranker;
import com.Harsh.Productivity.Os.ai.embedding.RrfOnlyCandidateReranker;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.scoring.onnx.OnnxScoringModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class RerankerConfiguration {
    @Bean
    @ConditionalOnProperty(name="ai.rag.reranker.enabled",havingValue ="true")
    public ScoringModel localRerankingScoringModel(@Value("${ai.rag.reranker.model-path}")String modelPath,@Value("${ai.rag.reranker.tokenizer-path}")String tokenizerPath){
        Path model = Paths.get(modelPath).toAbsolutePath();
        Path tokenizer = Paths.get(tokenizerPath).toAbsolutePath();
        if(!Files.isRegularFile(model)){
            throw new IllegalStateException("Reranker model file not found: "+model);
        }
        if(!Files.isRegularFile(tokenizer)){
            throw new IllegalStateException("Reranker tokenizer file not found: "+tokenizer);
        }
        return new OnnxScoringModel(model.toString(),tokenizer.toString());
    }
    @Bean
    @ConditionalOnProperty(name="ai.rag.reranker.enabled",havingValue ="true")
    public CandidateReranker localOnxxCandidateReranker(ScoringModel localRerankingScoringModel){
        return new LocalOnnxCandidateReranker(localRerankingScoringModel);
    }
    @Bean
    @ConditionalOnProperty(name = "ai.rag.reranker.enabled",havingValue = "false",matchIfMissing = true)
    public CandidateReranker rrfOnlyCandidateReranker(){
        return new RrfOnlyCandidateReranker();
    }
}
