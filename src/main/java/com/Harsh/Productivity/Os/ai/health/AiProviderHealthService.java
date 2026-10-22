package com.Harsh.Productivity.Os.ai.health;

import com.Harsh.Productivity.Os.ai.config.AiProvidersProperties;
import com.Harsh.Productivity.Os.ai.gateway.AiGateway;
import com.Harsh.Productivity.Os.ai.gateway.AiProvider;
import com.Harsh.Productivity.Os.ai.gateway.ProviderProbe;
import org.springframework.stereotype.Service;
import com.Harsh.Productivity.Os.ai.health.AiHealthResponse.ProviderHealth;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.ResponseEntity.ok;

@Service
public class AiProviderHealthService {
    private static final Duration DEEP_CACHE_TTL = Duration.ofMinutes(60);
    private record CachedProbe(ProviderProbe probe, Instant at){}
    private final AiGateway gateway;
    private final AiProvidersProperties props;
    private final Map<String,CachedProbe> inferenceCache=new ConcurrentHashMap<>();
    public AiProviderHealthService(AiGateway gateway, AiProvidersProperties props) {
        this.gateway = gateway;
        this.props = props;
    }
    public AiHealthResponse check(boolean deep){
        List<ProviderHealth> rows=new ArrayList<>();
        int configured=0;
        int usable=0;
        for(AiProvider provider: gateway.providers()){
            configured++;
            ProviderProbe connectivity=provider.probe();
            String circuit= gateway.circuitState(provider.id());
            if(connectivity.ok()&&!"OPEN".equals(circuit)){
                usable++;
            }
            Boolean inferenceOk=null;
            String inferenceDetail=null;
            if(deep){
                ProviderProbe inference=cachedInference(provider);
                inferenceOk=inference.ok();
                inferenceDetail=inference.detail();
            }
            rows.add(new ProviderHealth(provider.id(), provider.modelName(), true,circuit,connectivity.ok(),connectivity.latencyMs(),connectivity.detail(),inferenceOk,inferenceDetail));
        }
        addIfMissing(rows, "ollama", props.ollama() == null ? null : props.ollama().model(), "Disabled in configuration");
        addIfMissing(rows, "fallback", props.fallback() == null ? null : props.fallback().model(), "Disabled in configuration");
        String status = (configured == 0 || usable == 0) ? "DOWN" : (usable == configured ? "UP" : "DEGRADED");
        return new AiHealthResponse(status, rows);
    }
    private ProviderProbe cachedInference(AiProvider provider) {
        CachedProbe cached = inferenceCache.get(provider.id());
        if (cached != null && Duration.between(cached.at(), Instant.now()).compareTo(DEEP_CACHE_TTL) < 0) {
            return cached.probe();
        }
        ProviderProbe fresh = provider.probeInference();
        inferenceCache.put(provider.id(), new CachedProbe(fresh, Instant.now()));
        return fresh;
    }
    private void addIfMissing(List<ProviderHealth> rows, String id, String model, String reason) {
        boolean present = rows.stream().anyMatch(r -> r.id().equals(id));
        if (!present) {
            rows.add(new ProviderHealth(id, model, false, null, null, null, reason, null, null));
        }
    }
}
