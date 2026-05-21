package com.pms.repository;

import com.pms.entity.Property;
import com.pms.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByPropertyType(PropertyType propertyType);

    List<Property> findByIsActiveTrue();

    boolean existsByNameAndPropertyType(String name, PropertyType propertyType);
}
