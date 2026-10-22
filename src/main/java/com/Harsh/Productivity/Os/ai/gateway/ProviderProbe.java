package com.Harsh.Productivity.Os.ai.gateway;

/** Result of a provider check. 'detail' is always safe to show to a client (no secrets, no stack traces). */
public record ProviderProbe(boolean ok, long latencyMs, String detail) {

    public static ProviderProbe notProbed() {
        return new ProviderProbe(true, 0, "Not probed");
    }
}
