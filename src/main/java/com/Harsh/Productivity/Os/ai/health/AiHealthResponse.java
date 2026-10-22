package com.Harsh.Productivity.Os.ai.health;

import java.util.List;

public record AiHealthResponse(String status, List<ProviderHealth> providers){
    public record ProviderHealth(String id,String model,boolean configured,String ciruit,Boolean reachable,Long latencyMs,String detail,Boolean inferenceOk,String inferenceDetail){}
}
