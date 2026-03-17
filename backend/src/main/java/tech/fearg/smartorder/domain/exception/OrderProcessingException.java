package tech.fearg.smartorder.domain.exception;

/**
 * Domain-level exception for failures during order processing.
 * Unchecked — callers are not forced to handle it, staying clean of try/catch boilerplate.
 */
public class OrderProcessingException extends RuntimeException {

    public OrderProcessingException(String message) {
        super(message);
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
