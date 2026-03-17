package tech.fearg.smartorder.infrastructure.adapter.in.web.dto;

import tech.fearg.smartorder.domain.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
    public static OrderItemResponse fromDomain(OrderItem item) {
        return new OrderItemResponse(
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.totalPrice()
        );
    }
}
