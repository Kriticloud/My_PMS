package com.pms.strategy;

import com.pms.dto.BookingDTO;
import com.pms.entity.Booking;
import com.pms.entity.PropertyType;
import com.pms.entity.Room;
import com.pms.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Rental / Apartment PMS Strategy.
 * Long-term leases, monthly rent tracking, no daily booking.
 * Flow: LEASE_CREATED → ACTIVE → TERMINATED
 */
@Component
public class RentalStrategy implements PropertyStrategy {

    private static final Map<String, List<String>> TRANSITIONS = Map.of(
            "LEASE_CREATED", List.of("ACTIVE", "CANCELLED"),
            "ACTIVE", List.of("TERMINATED"),
            "TERMINATED", List.of(),
            "CANCELLED", List.of());

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.RENTAL;
    }

    @Override
    public String getUnitLabel() {
        return "Flat/Unit";
    }

    @Override
    public List<String> getValidStatuses() {
        return List.of("LEASE_CREATED", "ACTIVE", "TERMINATED", "CANCELLED");
    }

    @Override
    public String getInitialStatus() {
        return "LEASE_CREATED";
    }

    @Override
    public Map<String, String> getStatusActions() {
        return Map.of(
                "Activate Lease", "ACTIVE",
                "Terminate Lease", "TERMINATED",
                "Cancel", "CANCELLED");
    }

    @Override
    public boolean canTransition(String fromStatus, String toStatus) {
        List<String> allowed = TRANSITIONS.get(fromStatus);
        return allowed != null && allowed.contains(toStatus);
    }

    @Override
    public void validateBooking(BookingDTO dto) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            throw new BadRequestException("Lease start and end dates are required");
        }
        long months = ChronoUnit.MONTHS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        if (months < 1) {
            throw new BadRequestException("Rental leases require a minimum duration of 1 month");
        }
    }

    @Override
    public void onStatusChange(Booking booking, String newStatus, Room room) {
        switch (newStatus) {
            case "ACTIVE" -> room.setStatus("OCCUPIED");
            case "TERMINATED", "CANCELLED" -> room.setStatus("AVAILABLE");
        }
    }

    @Override
    public List<String> getRoomStatuses() {
        return List.of("AVAILABLE", "OCCUPIED", "MAINTENANCE");
    }

    @Override
    public boolean supportsPOS() {
        return false;
    }

    @Override
    public String getOccupiedRoomStatus() {
        return "OCCUPIED";
    }

    @Override
    public String getReleasedRoomStatus() {
        return "AVAILABLE";
    }
}
