package tech.fearg.smartorder.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregate Root of the domain.
 *
 * @Value  → all fields are private+final; equals/hashCode/toString generated.
 * @Builder → fluent builder API for construction.
 * Zero framework dependencies — this class belongs strictly to the domain.
 */
@Value
@Builder
public class Order {

    OrderId id;
    Client client;
    List<OrderItem> items;
    String rawInput;
    OrderStatus status;
    LocalDateTime createdAt;

    public Order(OrderId id, Client client, List<OrderItem> items,
                 String rawInput, OrderStatus status, LocalDateTime createdAt) {
        if (id == null) throw new IllegalArgumentException("OrderId must not be null");
        if (client == null) throw new IllegalArgumentException("Client must not be null");
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("An order must contain at least one item");
        this.id = id;
        this.client = client;
        this.items = List.copyOf(items); // defensive copy — enforce immutability
        this.rawInput = rawInput;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /** Domain behaviour: total monetary value of the order. */
    public BigDecimal totalAmount() {
        return items.stream()
                .map(OrderItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Factory — creates a new PENDING order. */
    public static Order newPending(Client client, List<OrderItem> items, String rawInput) {
        return Order.builder()
                .id(OrderId.generate())
                .client(client)
                .items(items)
                .rawInput(rawInput)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
