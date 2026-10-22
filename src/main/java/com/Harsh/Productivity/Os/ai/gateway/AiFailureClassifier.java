package com.Harsh.Productivity.Os.ai.gateway;

import com.Harsh.Productivity.Os.ai.exception.AiResponseException;
import dev.langchain4j.exception.InternalServerException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.service.output.OutputParsingException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

public final class AiFailureClassifier {
    public static AiFailure classify(Throwable error) {
        int depth = 0;
        for (Throwable t = error; t != null; t = t.getCause(), depth++) {
            if (t instanceof AiResponseException || t instanceof OutputParsingException) {
                return AiFailure.BAD_OUTPUT;
            }
            if (t instanceof dev.langchain4j.exception.TimeoutException || t instanceof HttpTimeoutException || t instanceof SocketTimeoutException || t instanceof java.util.concurrent.TimeoutException) {
                return AiFailure.FALLBACK;
            }
            if (t instanceof RateLimitException) {
                return AiFailure.FALLBACK;
            }
            if (t instanceof InternalServerException || t instanceof IOException) {
                return AiFailure.RETRY;
            }
        }
        return AiFailure.FALLBACK;
    }
}

