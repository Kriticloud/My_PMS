package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private Long id;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Room type ID is required")
    private Long roomTypeId;

    private String roomTypeName;
    private java.math.BigDecimal basePrice;

    @Min(value = 1, message = "Floor must be at least 1")
    private Integer floor;

    private String status;

    private Long propertyId;
    private String propertyName;
    private String propertyType;
    private Integer capacity;
    private Integer occupiedCount;
    private String wardName;
    private String unitLabel;
}
