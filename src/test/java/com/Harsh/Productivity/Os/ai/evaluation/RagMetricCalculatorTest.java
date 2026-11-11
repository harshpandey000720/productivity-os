package com.Harsh.Productivity.Os.ai.evaluation;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class RagMetricCalculatorTest {
    @Test
    void calculatesPrecisionRecallAndMrrAtK(){
        RetrievalMetrics metrics=RagMetricCalculator.calculate(
                List.of("doc:wrong", "doc:relevant-a", "doc:relevant-b"),
                Set.of("doc:relevant-a", "doc:relevant-b"),
                3,
                12
        );
        assertEquals(2.0 / 3.0, metrics.precisionAtK(), 1e-9);
        assertEquals(1.0, metrics.recallAtK(), 1e-9);
        assertEquals(0.5,metrics.mrrAtK(),1e-9);
        assertEquals(12,metrics.latencyMillis());
    }
    @Test
    void doesNotCountDuplicateResultsTwice(){
        RetrievalMetrics metrics=RagMetricCalculator.calculate(
                List.of("doc:relevant", "doc:relevant","doc:wrong"),
                Set.of("doc:relevant"),
                3,
                0
        );
        assertEquals(1.0 / 3.0, metrics.precisionAtK(), 1e-9);
        assertEquals(1.0, metrics.recallAtK(), 1e-9);
        assertEquals(1.0, metrics.mrrAtK(), 1e-9);
    }
    @Test
    void rejectAnEvaluationCaseWithoutRelevantChunks(){
        assertThrows(IllegalArgumentException.class, () -> RagMetricCalculator.calculate(List.of("doc:1"),Set.of(),3,0));
    }
}
