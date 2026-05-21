package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosOrderItemDTO {

    private Long id;

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    private String menuItemName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String notes;
}
