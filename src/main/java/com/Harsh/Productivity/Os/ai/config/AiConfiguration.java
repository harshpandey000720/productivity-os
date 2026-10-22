package com.Harsh.Productivity.Os.ai.config;

import com.Harsh.Productivity.Os.ai.gateway.AiGateway;
import com.Harsh.Productivity.Os.ai.gateway.AiProvider;
import com.Harsh.Productivity.Os.ai.provider.FallbackProvider;
import com.Harsh.Productivity.Os.ai.provider.OllamaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AiProvidersProperties.class)
public class AiConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AiGateway.class);

    @Bean
    public AiGateway aiGateway(AiProvidersProperties props) {
        Map<String, AiProvider> available = new LinkedHashMap<>();
        if (props.ollama() != null) {
            available.put("ollama", new OllamaProvider(props.ollama()));
        }
        if (props.fallback() != null) {
            available.put("fallback", new FallbackProvider(props.fallback()));
        }
        List<AiProvider> ordered = new ArrayList<>();
        for (String raw : props.priority()) {
            String id = raw.trim().toLowerCase();
            AiProvider provider = available.get(id);
            if (provider == null) {
                log.warn("AI provider '{}' is listed in ai.providers.priority but is not configured; skipping it", id);
            } else if (!ordered.contains(provider)) {
                ordered.add(provider);
            }
        }
        log.info("AI providers registered (priority order): {}",
                ordered.stream().map(p -> p.id() + "(" + p.modelName() + ")").toList());
        return new AiGateway(ordered, props.resilience());
    }
}
