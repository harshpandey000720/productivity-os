package com.Harsh.Productivity.Os.ai.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "ai.providers")
public record AiProvidersProperties(List<String> priority, Ollama ollama, Fallback fallback, Resilience resilience) {
    /** Missing config must never stop the app from booting: AI just becomes unavailable. */
    public AiProvidersProperties {
        if (priority == null || priority.isEmpty()) {
            priority = List.of("ollama", "fallback");
        }
        if (resilience == null) {
            resilience = Resilience.defaults();
        }
    }
    public record Ollama(String type, String baseUrl, String model, Duration timeout,double temperature) {}
    public record Fallback(String type, String baseUrl, String model, Duration timeout,double temperature) {}
    public record Resilience(int maxAttempts, Duration retryBackoff, int breakerWindowSize,int breakerMinimumCalls, float breakerFailureRate, Duration breakerOpenDuration) {
        public static Resilience defaults() {
            return new Resilience(2, Duration.ofMillis(300), 4, 2, 50f, Duration.ofSeconds(30));
        }
    }
}