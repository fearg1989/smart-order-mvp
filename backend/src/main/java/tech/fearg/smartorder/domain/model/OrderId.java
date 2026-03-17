package tech.fearg.smartorder.domain.model;

import lombok.Value;

import java.util.UUID;

/**
 * Value Object that wraps the identity of an Order.
 * @Value makes the class final, all fields private+final, generates
 * equals/hashCode/toString and an all-args constructor.
 */
@Value
public class OrderId {

    UUID value;

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(String raw) {
        return new OrderId(UUID.fromString(raw));
    }
}
