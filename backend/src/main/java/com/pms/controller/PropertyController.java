package com.pms.controller;

import com.pms.dto.PropertyDTO;
import com.pms.entity.PropertyType;
import com.pms.service.PropertyService;
import com.pms.strategy.PropertyStrategy;
import com.pms.strategy.PropertyStrategyFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyStrategyFactory strategyFactory;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RESTAURANT_STAFF')")
    public ResponseEntity<List<PropertyDTO>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getActiveProperties());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> createProperty(@Valid @RequestBody PropertyDTO dto) {
        return ResponseEntity.ok(propertyService.createProperty(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id, @RequestBody PropertyDTO dto) {
        return ResponseEntity.ok(propertyService.updateProperty(id, dto));
    }

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RESTAURANT_STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getPropertyTypes() {
        List<Map<String, Object>> types = Arrays.stream(PropertyType.values())
                .map(pt -> {
                    PropertyStrategy s = strategyFactory.getStrategy(pt);
                    return Map.<String, Object>of(
                            "type", pt.name(),
                            "unitLabel", s.getUnitLabel(),
                            "validStatuses", s.getValidStatuses(),
                            "statusActions", s.getStatusActions(),
                            "posSupported", s.supportsPOS(),
                            "roomStatuses", s.getRoomStatuses());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }
}
