package com.Harsh.Productivity.Os.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handles 400 Bad Request (Triggers automatically when @Valid payload fails)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleBadRequest(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid input data: Please check your fields.");
    }
    // Handles 409 Conflict (Triggers when your service throws your custom duplicate exception)
    @ExceptionHandler(TaskAlreadyExistsException.class)
    public ResponseEntity<String> handleConflict(TaskAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }
    //Handles 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
    //Handles 403 Access denied
    @ExceptionHandler(TaskAccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(TaskAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }
}

