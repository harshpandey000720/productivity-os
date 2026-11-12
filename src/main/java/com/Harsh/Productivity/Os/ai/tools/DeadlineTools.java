package com.Harsh.Productivity.Os.ai.tools;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.exception.ToolExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
public class DeadlineTools {
    private static final Logger logger = LoggerFactory.getLogger(DeadlineTools.class);
    private final Clock clock;
    public  DeadlineTools() {
        this(Clock.systemDefaultZone());
    }
    DeadlineTools(Clock clock) {
        this.clock = clock;
    }
    @Tool("Returns how many days remain until a deadline date. A negative number means the deadline has already "
            + "passed. Use this for any question about days left, days remaining or overdue.")
    public long daysUntil(@P("The deadline as an ISO date, for example 2026-12-31")String deadline) {
        LocalDate date;
        try {
            date = LocalDate.parse(deadline==null?"":deadline.trim());
        }catch (DateTimeParseException e){
            logger.error("Failed to parse deadline date: {}", deadline, e);
            throw new ToolExecutionException("The date must be written as yyyy-MM-dd, for example 2026-12-31.");
        }
        long days=ChronoUnit.DAYS.between(LocalDate.now(clock), date);
        logger.info("tools=daysUntil deadline={} result={}",date,days);
        return days;
    }
}
