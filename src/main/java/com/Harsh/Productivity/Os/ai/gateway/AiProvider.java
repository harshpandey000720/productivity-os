package com.Harsh.Productivity.Os.ai.gateway;

public interface AiProvider {
    String id();
    String modelName();
    <S> S service(Class<S> serviceType);
    default ProviderProbe probe() {
        return ProviderProbe.notProbed();
    }
    default ProviderProbe probeInference() {
        return ProviderProbe.notProbed();
    }
}
