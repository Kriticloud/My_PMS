package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {

    private Long id;

    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private String description;
    private Boolean isAvailable;
    private Integer stockQuantity;
}
