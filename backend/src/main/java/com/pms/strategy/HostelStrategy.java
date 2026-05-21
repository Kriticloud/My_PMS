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
 * Hostel / PG PMS Strategy.
 * Long-term monthly stays, bed-level management, shared rooms.
 * Flow: BOOKED → ACTIVE → VACATED
 */
@Component
public class HostelStrategy implements PropertyStrategy {

    private static final Map<String, List<String>> TRANSITIONS = Map.of(
            "BOOKED", List.of("ACTIVE", "CANCELLED"),
            "ACTIVE", List.of("VACATED"),
            "VACATED", List.of(),
            "CANCELLED", List.of());

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.HOSTEL;
    }

    @Override
    public String getUnitLabel() {
        return "Bed";
    }

    @Override
    public List<String> getValidStatuses() {
        return List.of("BOOKED", "ACTIVE", "VACATED", "CANCELLED");
    }

    @Override
    public String getInitialStatus() {
        return "BOOKED";
    }

    @Override
    public Map<String, String> getStatusActions() {
        return Map.of(
                "Activate", "ACTIVE",
                "Vacate", "VACATED",
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
            throw new BadRequestException("Start and end dates are required for hostel bookings");
        }
        long days = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        if (days < 28) {
            throw new BadRequestException("Hostel bookings require a minimum stay of 28 days");
        }
    }

    @Override
    public void onStatusChange(Booking booking, String newStatus, Room room) {
        switch (newStatus) {
            case "ACTIVE" -> {
                room.setOccupiedCount(room.getOccupiedCount() + 1);
                if (room.getOccupiedCount() >= room.getCapacity()) {
                    room.setStatus("OCCUPIED");
                }
            }
            case "VACATED", "CANCELLED" -> {
                room.setOccupiedCount(Math.max(0, room.getOccupiedCount() - 1));
                if (room.getOccupiedCount() == 0) {
                    room.setStatus("AVAILABLE");
                }
            }
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
