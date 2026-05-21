package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosOrderDTO {

    private Long id;
    private String orderNumber;

    private Long bookingId;
    private String guestName;
    private String roomNumber;

    @NotBlank(message = "Order type is required")
    private String orderType;

    private String status;
    private BigDecimal totalAmount;

    @NotEmpty(message = "Order must have at least one item")
    private List<PosOrderItemDTO> items;

    private String createdAt;
}
