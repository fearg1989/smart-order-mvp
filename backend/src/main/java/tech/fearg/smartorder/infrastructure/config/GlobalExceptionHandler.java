package tech.fearg.smartorder.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.fearg.smartorder.domain.exception.OrderProcessingException;
import java.util.stream.Collectors;

/**
 * Global Exception Handler — translates domain and application exceptions
 * to meaningful HTTP responses.
 *
 * Centralizing error handling here keeps controllers clean (no try/catch blocks).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 422 — AI parsing failed or order invariants violated. */
    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<ErrorResponse> handleOrderProcessing(OrderProcessingException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 400 — Bean validation failure from @Valid on @RequestBody. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }

    /** 400 — Invalid input (e.g., blank rawText, missing clientId). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 500 — Unexpected errors — never expose internal details to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected internal error occurred. Please try again."));
    }
}
