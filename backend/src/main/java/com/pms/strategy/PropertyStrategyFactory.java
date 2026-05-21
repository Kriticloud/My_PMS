package com.pms.strategy;

import com.pms.entity.PropertyType;
import com.pms.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory to resolve the correct PropertyStrategy based on PropertyType.
 * Uses Spring DI to collect all strategy beans automatically.
 */
@Component
public class PropertyStrategyFactory {

    private final Map<PropertyType, PropertyStrategy> strategies;

    public PropertyStrategyFactory(List<PropertyStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PropertyStrategy::getPropertyType, Function.identity()));
    }

    public PropertyStrategy getStrategy(PropertyType propertyType) {
        PropertyStrategy strategy = strategies.get(propertyType);
        if (strategy == null) {
            throw new BadRequestException("Unsupported property type: " + propertyType);
        }
        return strategy;
    }

    public PropertyStrategy getStrategy(String propertyType) {
        return getStrategy(PropertyType.valueOf(propertyType.toUpperCase()));
    }
}
