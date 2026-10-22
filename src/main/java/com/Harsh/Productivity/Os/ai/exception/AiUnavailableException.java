package com.Harsh.Productivity.Os.ai.exception;

/** Every configured provider failed (or none is configured). Mapped to HTTP 503. */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message) {
        super(message);
    }
}
