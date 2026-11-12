package com.Harsh.Productivity.Os.ai.tools;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
@EnabledIfEnvironmentVariable(named = "OLLAMA_TOOL_TESTS", matches = "true")
public class ToolCallingSmokeTest {
    interface DeadlineAssistant{
        Result<String> chat(String message);
    }
    private static DeadlineAssistant assistant(String modelName) {
        ChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(modelName)
                .temperature(0.0)
                .timeout(Duration.ofSeconds(180))
                .logRequests(true)
                .logResponses(true)
                .build();
        return AiServices.builder(DeadlineAssistant.class)
                .chatModel(model)
                .tools(new DeadlineTools())
                .toolArgumentsErrorHandler((error, context) ->
                        ToolErrorHandlerResult.text(error.getMessage()))
                .toolExecutionErrorHandler((error, context) -> {
                    throw new RuntimeException(error);
                })
                .build();
    }
    @ParameterizedTest
    @ValueSource(strings = {"qwen3:4b-instruct-2507-q4_K_M", "llama3.2:latest"})
    void callsTheDeadlineToolForADateQuestion(String modelName) {
        LocalDate deadline = LocalDate.now().plusDays(45);
        long started = System.nanoTime();
        Result<String> result = assistant(modelName).chat("How many days are left until " + deadline + "?");
        long seconds = (System.nanoTime() - started) / 1_000_000_000;
        System.out.println("== " + modelName + " (" + seconds + "s) ==");
        System.out.println("answer: " + result.content());
        result.toolExecutions().forEach(t -> System.out.println(
                "tool: " + t.request().name() + " args=" + t.request().arguments() + " result=" + t.result()));
        // A failure here is a finding about the MODEL, not necessarily a bug in your code. Record it.
        assertFalse(result.toolExecutions().isEmpty(), modelName + " did not call the tool");
        long expected = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        assertTrue(result.content().contains(String.valueOf(expected)),
                modelName + " answered: " + result.content());
    }
    @ParameterizedTest
    @ValueSource(strings = {"llama3.2:latest", "qwen3:4b-instruct-2507-q4_K_M"})
    void handlesInvalidDateFormat(String modelName) {
        Result<String> response = assistant(modelName)
                .chat("How many days until next Friday?");

        assertTrue(response.content() != null && !response.content().isBlank());
    }
}
