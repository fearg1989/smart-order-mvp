package tech.fearg.smartorder.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;
import tech.fearg.smartorder.domain.model.Client;
import tech.fearg.smartorder.domain.model.Order;
import tech.fearg.smartorder.domain.model.OrderId;
import tech.fearg.smartorder.domain.model.OrderItem;

import java.util.List;

/**
 * Anti-corruption layer between the domain model and the JPA model.
 *
 * Neither the domain nor Spring Data JPA depends on each other directly;
 * this mapper bridges both worlds without polluting either.
 */
@Component
public class OrderMapper {

    public OrderJpaEntity toJpaEntity(Order order) {
        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
                .map(this::toItemJpaEntity)
                .toList();

        return OrderJpaEntity.builder()
                .id(order.getId().getValue())
                .clientId(order.getClient().getId())
                .clientName(order.getClient().getName())
                .clientEmail(order.getClient().getEmail())
                .items(itemEntities)
                .rawInput(order.getRawInput())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public Order toDomain(OrderJpaEntity entity) {
        Client client = new Client(entity.getClientId(), entity.getClientName(), entity.getClientEmail());

        List<OrderItem> items = entity.getItems().stream()
                .map(this::toItemDomain)
                .toList();

        return Order.builder()
                .id(new OrderId(entity.getId()))
                .client(client)
                .items(items)
                .rawInput(entity.getRawInput())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private OrderItemJpaEntity toItemJpaEntity(OrderItem item) {
        return OrderItemJpaEntity.builder()
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }

    private OrderItem toItemDomain(OrderItemJpaEntity entity) {
        return new OrderItem(entity.getProductName(), entity.getQuantity(), entity.getUnitPrice());
    }
}
