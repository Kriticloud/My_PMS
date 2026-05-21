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
 * Hotel PMS Strategy.
 * Short-term daily stays, room-level booking, walk-ins allowed.
 * Flow: BOOKED → CHECKED_IN → CHECKED_OUT
 */
@Component
public class HotelStrategy implements PropertyStrategy {

    private static final Map<String, List<String>> TRANSITIONS = Map.of(
            "BOOKED", List.of("CHECKED_IN", "CANCELLED"),
            "CHECKED_IN", List.of("CHECKED_OUT"),
            "CHECKED_OUT", List.of(),
            "CANCELLED", List.of());

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.HOTEL;
    }

    @Override
    public String getUnitLabel() {
        return "Room";
    }

    @Override
    public List<String> getValidStatuses() {
        return List.of("BOOKED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED");
    }

    @Override
    public String getInitialStatus() {
        return "BOOKED";
    }

    @Override
    public Map<String, String> getStatusActions() {
        return Map.of(
                "Check In", "CHECKED_IN",
                "Check Out", "CHECKED_OUT",
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
            throw new BadRequestException("Check-in and check-out dates are required for hotel bookings");
        }
        if (!dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }
        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        if (nights > 365) {
            throw new BadRequestException("Hotel bookings cannot exceed 365 nights");
        }
    }

    @Override
    public void onStatusChange(Booking booking, String newStatus, Room room) {
        switch (newStatus) {
            case "CHECKED_IN" -> room.setStatus("OCCUPIED");
            case "CHECKED_OUT" -> room.setStatus("CLEANING");
            case "CANCELLED" -> {
                /* room stays as-is */ }
        }
    }

    @Override
    public List<String> getRoomStatuses() {
        return List.of("AVAILABLE", "OCCUPIED", "CLEANING", "MAINTENANCE");
    }

    @Override
    public boolean supportsPOS() {
        return true;
    }

    @Override
    public String getOccupiedRoomStatus() {
        return "OCCUPIED";
    }

    @Override
    public String getReleasedRoomStatus() {
        return "CLEANING";
    }
}
