package com.pms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDTO {

    private Long id;

    @NotBlank(message = "Property name is required")
    private String name;

    @NotNull(message = "Property type is required")
    private String propertyType;

    private String address;
    private String contactPhone;
    private String contactEmail;
    private Boolean isActive;

    private java.util.List<String> validStatuses;
    private java.util.Map<String, String> statusActions;
    private String unitLabel;
    private boolean posSupported;
}
