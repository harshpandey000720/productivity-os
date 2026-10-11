package com.Harsh.Productivity.Os.ai.exception;

public class AiResponseException extends RuntimeException{
    public AiResponseException(String message){
        super(message);
    }
    public AiResponseException(String message, Throwable cause){
        super(message,cause);
    }
    public AiResponseException(Throwable cause){
        super(cause);
    }
}
