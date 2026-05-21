package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private Long id;
    private String bookingNumber;

    @NotNull(message = "Guest ID is required")
    private Long guestId;

    private String guestName;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    private String roomNumber;
    private String roomTypeName;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    private Integer numGuests;
    private String status;
    private String specialRequests;
    private java.math.BigDecimal totalAmount;
    private String actualCheckIn;
    private String actualCheckOut;
}
