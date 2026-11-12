package com.Harsh.Productivity.Os.ai.tools;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class DeadlineToolsTest {
    private final DeadlineTools tools = new DeadlineTools(Clock.fixed(Instant.parse("2026-11-10T00:00:00Z"), ZoneOffset.UTC));
    @Test
    void countsDaysUntilAFutureDeadline() {
        assertEquals(21, tools.daysUntil("2026-12-01"));
    }
    @Test
    void returnsZeroOnTheDeadlineDay() {
        assertEquals(0, tools.daysUntil("2026-11-10"));
    }
    @Test
    void returnsNegativeWhenTheDeadlineHasPassed() {
        assertEquals(-5, tools.daysUntil("2026-11-05"));
    }
    @Test
    void trimsWhitespace() {
        assertEquals(21, tools.daysUntil("  2026-12-01 "));
    }
    @Test
    void rejectsInvalidAndMissingDates() {
        assertThrows(RuntimeException.class, () -> tools.daysUntil("next friday"));
        assertThrows(RuntimeException.class, () -> tools.daysUntil(""));
        assertThrows(RuntimeException.class, () -> tools.daysUntil(null));
    }
}