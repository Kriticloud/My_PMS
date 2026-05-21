package com.pms.strategy;

import com.pms.dto.BookingDTO;
import com.pms.entity.Booking;
import com.pms.entity.PropertyType;
import com.pms.entity.Room;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for property-type-specific business logic.
 * Each property type implements its own booking lifecycle,
 * validation rules, and operational behavior.
 */
public interface PropertyStrategy {

    PropertyType getPropertyType();

    /** Human-readable label for bookable units (Room, Bed, Ward/Bed, Flat, Room) */
    String getUnitLabel();

    /** Ordered list of valid booking statuses for this property type */
    List<String> getValidStatuses();

    /** The initial status when a booking is created */
    String getInitialStatus();

    /** Map of action name → target status (e.g., "Check In" → "CHECKED_IN") */
    Map<String, String> getStatusActions();

    /** Whether the transition from one status to another is allowed */
    boolean canTransition(String fromStatus, String toStatus);

    /** Validate a booking DTO before creation (property-specific rules) */
    void validateBooking(BookingDTO dto);

    /** Handle side effects when a booking transitions to a new status */
    void onStatusChange(Booking booking, String newStatus, Room room);

    /** Room statuses applicable to this property type */
    List<String> getRoomStatuses();

    /** Whether POS integration is supported */
    boolean supportsPOS();

    /** Room status to set when a unit is allocated */
    String getOccupiedRoomStatus();

    /** Room status to set when a unit is released */
    String getReleasedRoomStatus();
}
