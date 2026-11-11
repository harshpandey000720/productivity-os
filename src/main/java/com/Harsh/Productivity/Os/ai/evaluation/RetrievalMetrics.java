package com.Harsh.Productivity.Os.ai.evaluation;

public record RetrievalMetrics(double precisionAtK,double recallAtK,double mrrAtK,long latencyMillis){
}
