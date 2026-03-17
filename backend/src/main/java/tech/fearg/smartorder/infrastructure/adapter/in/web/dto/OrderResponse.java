package tech.fearg.smartorder.infrastructure.adapter.in.web.dto;

import tech.fearg.smartorder.domain.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String orderId,
        String clientId,
        String clientName,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt
) {
    public static OrderResponse fromDomain(Order order) {
        return new OrderResponse(
                order.getId().getValue().toString(),
                order.getClient().getId(),
                order.getClient().getName(),
                order.getItems().stream().map(OrderItemResponse::fromDomain).toList(),
                order.totalAmount(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}
