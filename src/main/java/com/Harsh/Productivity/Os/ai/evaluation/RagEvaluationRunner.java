package com.Harsh.Productivity.Os.ai.evaluation;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
public class RagEvaluationRunner {
    public<T> RagEvaluationResult evaluate(
            String mode,
            RagEvaluationCase testCase,
            int k,
            Supplier<List<T>> retrieval,
            Function<T,String > chunkKey
    ){
        long start=System.nanoTime();
        List<T> results=retrieval.get();
        long latencyMillis=(System.nanoTime()-start)/1_000_000;
        List<String> keys=results.stream()
                .map(chunkKey)
                .toList();
        RetrievalMetrics metrics=RagMetricCalculator.calculate(
                keys,
                testCase.relevantChunkKeys(),
                k,
                latencyMillis
        );
        return new RagEvaluationResult(
                mode,
                testCase.caseId(),
                keys,
                metrics
        );
    }
}
