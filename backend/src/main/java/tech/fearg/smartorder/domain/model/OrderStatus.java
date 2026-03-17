package tech.fearg.smartorder.domain.model;

/**
 * Enum representing the lifecycle of a B2B Order.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
