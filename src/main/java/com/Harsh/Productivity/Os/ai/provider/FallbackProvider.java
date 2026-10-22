package com.Harsh.Productivity.Os.ai.provider;

import com.Harsh.Productivity.Os.ai.config.AiProvidersProperties;
import com.Harsh.Productivity.Os.ai.gateway.ProviderProbe;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class FallbackProvider extends ChatModelProvider{
    private final String baseUrl;
    public FallbackProvider(AiProvidersProperties.Fallback cfg){
        super("fallback", cfg.model(), OllamaChatModel.builder()
                .baseUrl(cfg.baseUrl())
                .modelName(cfg.model())
                .temperature(cfg.temperature())
                .timeout(cfg.timeout())
                .maxRetries(0) // the gateway owns retries; hidden client retries would multiply with them
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .build());
        this.baseUrl = stripTailingSlash(cfg.baseUrl());
    }
    @Override
    public ProviderProbe probe() {
        return httpGet(baseUrl+"/api/tags",null,modelName());
    }
}
