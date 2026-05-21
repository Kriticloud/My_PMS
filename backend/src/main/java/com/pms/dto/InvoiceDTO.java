package com.pms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {

    private Long id;
    private String invoiceNumber;
    private Long bookingId;
    private String bookingNumber;
    private Long guestId;
    private String guestName;
    private String roomNumber;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String status;
    private String generatedAt;
    private String notes;
    private List<InvoiceItemDTO> items;
}
