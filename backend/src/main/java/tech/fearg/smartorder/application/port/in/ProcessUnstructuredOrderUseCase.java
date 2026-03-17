package tech.fearg.smartorder.application.port.in;

import tech.fearg.smartorder.domain.model.Order;

/**
 * Inbound Port (Primary Port / Driving Port).
 *
 * This is the entry point to the application core. The Infrastructure layer
 * (REST controller, CLI, etc.) depends on this interface, never on the concrete service.
 *
 * Pattern: Use Case Interface — one interface per use case.
 */
public interface ProcessUnstructuredOrderUseCase {

    /**
     * Accepts a raw, unstructured B2B order text, parses it with AI,
     * persists the result, and returns the structured domain Order.
     *
     * @param command encapsulates all input data
     * @return the persisted domain Order
     */
    Order process(ProcessOrderCommand command);
}
