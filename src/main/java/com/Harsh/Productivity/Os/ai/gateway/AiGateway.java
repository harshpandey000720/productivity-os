package com.Harsh.Productivity.Os.ai.gateway;
import com.Harsh.Productivity.Os.ai.config.AiProvidersProperties;
import com.Harsh.Productivity.Os.ai.exception.AiResponseException;
import com.Harsh.Productivity.Os.ai.exception.AiUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
public class AiGateway {
    private static final Logger log= LoggerFactory.getLogger(AiGateway.class);
    private final List<AiProvider> providers;
    private final Map<String, CircuitBreaker> breakers=new LinkedHashMap<>();
    private final int maxAttempts;
    private final Duration retryBackoff;
    public AiGateway(List<AiProvider> providers, AiProvidersProperties.Resilience cfg) {
        this.providers = List.copyOf(providers);
        this.maxAttempts=Math.max(1, cfg.maxAttempts());
        this.retryBackoff=cfg.retryBackoff();
        CircuitBreakerConfig breakerConfig=CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(cfg.breakerWindowSize())
                .minimumNumberOfCalls(cfg.breakerMinimumCalls())
                .failureRateThreshold(cfg.breakerFailureRate())
                .waitDurationInOpenState(cfg.breakerOpenDuration())
                .permittedNumberOfCallsInHalfOpenState(1)
                .recordException(e-> AiFailureClassifier.classify(e)!= AiFailure.BAD_OUTPUT)
                .build();
        for (AiProvider provider : this.providers) {
            breakers.put(provider.id(),CircuitBreaker.of(provider.id(), breakerConfig));
        }
    }
    public <S,R> R call(Class<S>serviceType, Function<S,R> operation){
        if (providers.isEmpty()){
            throw new AiUnavailableException("No AI providers is configured");
        }
        List<String> failures=new ArrayList<>();
        boolean sawBadOutput=false;
        for (AiProvider provider : providers) {
            CircuitBreaker breaker = breakers.get(provider.id());
            for(int attempt=1;attempt<=maxAttempts;attempt++){
                try {
                    R result=breaker.executeSupplier(()->operation.apply(provider.service(serviceType)));
                    log.info("AI ca;; served by provider={} model={} attempt={}",provider.id(),provider.modelName(),attempt);
                    return result;
                }catch(CallNotPermittedException e){
                    log.warn("AI provider {} skipped:circuit breaker is open",provider.id());
                    failures.add(provider.id()+"=circuit-open");
                    break;
                }catch(RuntimeException e){
                    AiFailure kind=AiFailureClassifier.classify(e);
                    log.warn("AI provider {} failed on attempt {}/{} ({}):{}",provider.id(),attempt,maxAttempts,kind,e.getMessage());
                    log.debug("AI failure detail",e);
                    failures.add(provider.id()+"="+kind);
                    if(kind==AiFailure.BAD_OUTPUT){
                        sawBadOutput=true;
                    }
                    if(kind!=AiFailure.RETRY||attempt==maxAttempts){
                        break;
                    }
                    pause();
                }
            }
        }
        log.error("A;; AI providers failed:{}",failures);
        if(sawBadOutput){
            throw new AiResponseException("The AI returned an unusable response");
        }
        throw new AiUnavailableException("The AI service is temporarily unavailable");
    }
    public List<AiProvider> providers() {
        return providers;
    }
    public String circuitState(String providerId){
        CircuitBreaker breaker=breakers.get(providerId);
        return breaker==null?"UNKNOWN":breaker.getState().name();
    }
    private void pause(){
        try {
            Thread.sleep(retryBackoff.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiUnavailableException("The AI request was interrupted");
        }
    }
}
