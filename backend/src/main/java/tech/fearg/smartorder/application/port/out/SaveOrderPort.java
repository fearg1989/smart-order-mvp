package tech.fearg.smartorder.application.port.out;

import tech.fearg.smartorder.domain.model.Order;

/**
 * Outbound Port (Secondary Port / Driven Port) — Persistence side.
 *
 * The application core declares WHAT it needs for persistence.
 * The JPA adapter in the infrastructure layer provides the implementation.
 * The domain never touches Spring Data or Hibernate directly.
 */
public interface SaveOrderPort {

    /**
     * Persists the given Order and returns the saved instance
     * (which may include a DB-generated ID or timestamps if applicable).
     *
     * @param order the domain Order to persist
     * @return the persisted Order
     */
    Order save(Order order);
}
