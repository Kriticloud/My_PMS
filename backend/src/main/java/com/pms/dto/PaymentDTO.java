package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long id;

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    private String invoiceNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    private String transactionRef;
    private String status;
    private String paidAt;
}
