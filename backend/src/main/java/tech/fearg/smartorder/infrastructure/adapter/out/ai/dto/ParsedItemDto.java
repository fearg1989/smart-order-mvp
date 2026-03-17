package tech.fearg.smartorder.infrastructure.adapter.out.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedItemDto {
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
}
