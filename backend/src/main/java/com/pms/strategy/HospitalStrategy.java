package com.pms.strategy;

import com.pms.dto.BookingDTO;
import com.pms.entity.Booking;
import com.pms.entity.PropertyType;
import com.pms.entity.Room;
import com.pms.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Hospital PMS Strategy.
 * Bed allocation, ward management, patient admissions.
 * Flow: ADMITTED → UNDER_TREATMENT → DISCHARGED
 */
@Component
public class HospitalStrategy implements PropertyStrategy {

    private static final Map<String, List<String>> TRANSITIONS = Map.of(
            "ADMITTED", List.of("UNDER_TREATMENT", "DISCHARGED", "CANCELLED"),
            "UNDER_TREATMENT", List.of("DISCHARGED"),
            "DISCHARGED", List.of(),
            "CANCELLED", List.of());

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.HOSPITAL;
    }

    @Override
    public String getUnitLabel() {
        return "Ward/Bed";
    }

    @Override
    public List<String> getValidStatuses() {
        return List.of("ADMITTED", "UNDER_TREATMENT", "DISCHARGED", "CANCELLED");
    }

    @Override
    public String getInitialStatus() {
        return "ADMITTED";
    }

    @Override
    public Map<String, String> getStatusActions() {
        return Map.of(
                "Begin Treatment", "UNDER_TREATMENT",
                "Discharge", "DISCHARGED",
                "Cancel", "CANCELLED");
    }

    @Override
    public boolean canTransition(String fromStatus, String toStatus) {
        List<String> allowed = TRANSITIONS.get(fromStatus);
        return allowed != null && allowed.contains(toStatus);
    }

    @Override
    public void validateBooking(BookingDTO dto) {
        if (dto.getCheckInDate() == null) {
            throw new BadRequestException("Admission date is required for hospital admissions");
        }
    }

    @Override
    public void onStatusChange(Booking booking, String newStatus, Room room) {
        switch (newStatus) {
            case "ADMITTED", "UNDER_TREATMENT" -> {
                room.setOccupiedCount(room.getOccupiedCount() + (newStatus.equals("ADMITTED") ? 1 : 0));
                if (room.getOccupiedCount() >= room.getCapacity()) {
                    room.setStatus("OCCUPIED");
                }
            }
            case "DISCHARGED", "CANCELLED" -> {
                room.setOccupiedCount(Math.max(0, room.getOccupiedCount() - 1));
                if (room.getOccupiedCount() == 0) {
                    room.setStatus("SANITIZING");
                }
            }
        }
    }

    @Override
    public List<String> getRoomStatuses() {
        return List.of("AVAILABLE", "OCCUPIED", "SANITIZING", "MAINTENANCE");
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
        return "SANITIZING";
    }
}
