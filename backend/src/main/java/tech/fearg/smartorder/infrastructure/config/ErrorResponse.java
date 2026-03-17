package tech.fearg.smartorder.infrastructure.config;

/**
 * Uniform error payload returned by all exception handlers.
 */
public record ErrorResponse(String message) {}
