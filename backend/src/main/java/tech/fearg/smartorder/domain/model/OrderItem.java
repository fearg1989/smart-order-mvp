package tech.fearg.smartorder.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Value Object representing a single line-item in an Order.
 * Domain behaviour (totalPrice) lives here, keeping logic close to data.
 * @Value = immutable; @Builder = fluent construction.
 */
@Value
@Builder
public class OrderItem {

    String productName;
    int quantity;
    BigDecimal unitPrice;

    public OrderItem(String productName, int quantity, BigDecimal unitPrice) {
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("Product name must not be blank");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Unit price must not be negative");
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** Pure domain behaviour — no framework dependency. */
    public BigDecimal totalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
