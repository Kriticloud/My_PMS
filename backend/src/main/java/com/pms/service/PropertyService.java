package com.pms.service;

import com.pms.dto.PropertyDTO;
import com.pms.entity.Property;
import com.pms.entity.PropertyType;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.PropertyRepository;
import com.pms.strategy.PropertyStrategy;
import com.pms.strategy.PropertyStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyStrategyFactory strategyFactory;

    public List<PropertyDTO> getAllProperties() {
        return propertyRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<PropertyDTO> getActiveProperties() {
        return propertyRepository.findByIsActiveTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PropertyDTO getPropertyById(Long id) {
        return toDTO(findProperty(id));
    }

    @Transactional
    public PropertyDTO createProperty(PropertyDTO dto) {
        PropertyType type = PropertyType.valueOf(dto.getPropertyType().toUpperCase());
        if (propertyRepository.existsByNameAndPropertyType(dto.getName(), type)) {
            throw new BadRequestException("Property with this name and type already exists");
        }

        Property property = Property.builder()
                .name(dto.getName())
                .propertyType(type)
                .address(dto.getAddress())
                .contactPhone(dto.getContactPhone())
                .contactEmail(dto.getContactEmail())
                .isActive(true)
                .build();
        return toDTO(propertyRepository.save(property));
    }

    @Transactional
    public PropertyDTO updateProperty(Long id, PropertyDTO dto) {
        Property property = findProperty(id);
        if (dto.getName() != null)
            property.setName(dto.getName());
        if (dto.getAddress() != null)
            property.setAddress(dto.getAddress());
        if (dto.getContactPhone() != null)
            property.setContactPhone(dto.getContactPhone());
        if (dto.getContactEmail() != null)
            property.setContactEmail(dto.getContactEmail());
        if (dto.getIsActive() != null)
            property.setIsActive(dto.getIsActive());
        return toDTO(propertyRepository.save(property));
    }

    private Property findProperty(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }

    private PropertyDTO toDTO(Property p) {
        PropertyStrategy strategy = strategyFactory.getStrategy(p.getPropertyType());
        return PropertyDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .propertyType(p.getPropertyType().name())
                .address(p.getAddress())
                .contactPhone(p.getContactPhone())
                .contactEmail(p.getContactEmail())
                .isActive(p.getIsActive())
                .validStatuses(strategy.getValidStatuses())
                .statusActions(strategy.getStatusActions())
                .unitLabel(strategy.getUnitLabel())
                .posSupported(strategy.supportsPOS())
                .build();
    }
}
