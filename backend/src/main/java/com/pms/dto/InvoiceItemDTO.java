package com.pms.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemDTO {
    private Long id;
    private String description;
    private String itemType;
    private BigDecimal amount;
    private Long referenceId;
}
