package com.pms.service;

import com.pms.dto.BookingDTO;
import com.pms.entity.*;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.BookingRepository;
import com.pms.repository.GuestRepository;
import com.pms.repository.PropertyRepository;
import com.pms.repository.RoomRepository;
import com.pms.strategy.PropertyStrategy;
import com.pms.strategy.PropertyStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyStrategyFactory strategyFactory;
    private final WebSocketNotificationService wsNotificationService;

    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Page<BookingDTO> getBookingsPaged(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::toDTO);
    }

    public BookingDTO getBookingById(Long id) {
        return toDTO(findBooking(id));
    }

    public List<BookingDTO> getBookingsByGuest(Long guestId) {
        return bookingRepository.findByGuestId(guestId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO createBooking(BookingDTO dto) {
        Guest guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Resolve property type from the room's property or from the DTO
        PropertyType propertyType = resolvePropertyType(dto, room);
        PropertyStrategy strategy = strategyFactory.getStrategy(propertyType);

        // Delegate validation to the strategy
        strategy.validateBooking(dto);

        // Check availability based on property type
        if (room.getCapacity() <= 1) {
            // Exclusive occupancy (Hotel, Resort, Rental): check date overlap
            List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                    room.getId(), dto.getCheckInDate(), dto.getCheckOutDate());
            if (!overlapping.isEmpty()) {
                throw new BadRequestException("Unit is not available for the selected dates");
            }
        } else {
            // Shared occupancy (Hostel, Hospital): check capacity
            if (room.getOccupiedCount() >= room.getCapacity()) {
                throw new BadRequestException("No available beds in this unit");
            }
        }

        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(),
                dto.getCheckOutDate() != null ? dto.getCheckOutDate() : dto.getCheckInDate().plusDays(1));
        BigDecimal total = room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(Math.max(nights, 1)));

        Property property = room.getProperty();

        Booking booking = Booking.builder()
                .bookingNumber("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .guest(guest)
                .room(room)
                .property(property)
                .propertyType(propertyType)
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate() != null ? dto.getCheckOutDate() : dto.getCheckInDate().plusDays(1))
                .numGuests(dto.getNumGuests() != null ? dto.getNumGuests() : 1)
                .status(strategy.getInitialStatus())
                .specialRequests(dto.getSpecialRequests())
                .totalAmount(total)
                .build();

        return toDTO(bookingRepository.save(booking));
    }

    /**
     * Universal status transition — delegates to the property-specific strategy.
     */
    @Transactional
    public BookingDTO transitionStatus(Long id, String targetStatus) {
        Booking booking = findBooking(id);
        PropertyStrategy strategy = strategyFactory.getStrategy(booking.getPropertyType());

        if (!strategy.canTransition(booking.getStatus(), targetStatus)) {
            throw new BadRequestException(
                    "Cannot transition from " + booking.getStatus() + " to " + targetStatus
                            + " for " + booking.getPropertyType() + " property type");
        }

        String oldStatus = booking.getStatus();
        booking.setStatus(targetStatus);

        // Set timestamps for check-in/check-out style transitions
        if (strategy.getOccupiedRoomStatus().equals("OCCUPIED") && isAllocationStatus(targetStatus, strategy)) {
            booking.setActualCheckIn(LocalDateTime.now());
        }
        if (isReleaseStatus(targetStatus, strategy)) {
            booking.setActualCheckOut(LocalDateTime.now());
        }

        // Delegate room status side effects to the strategy
        Room room = booking.getRoom();
        strategy.onStatusChange(booking, targetStatus, room);
        roomRepository.save(room);

        BookingDTO result = toDTO(bookingRepository.save(booking));

        // WebSocket notifications
        wsNotificationService.notifyRoomStatusChange(room.getId(), room.getRoomNumber(), room.getStatus());
        wsNotificationService.notifyBookingUpdate(booking.getId(), booking.getBookingNumber(), targetStatus);
        wsNotificationService.notifyDashboardUpdate();

        return result;
    }

    private boolean isAllocationStatus(String status, PropertyStrategy strategy) {
        String initial = strategy.getInitialStatus();
        return !status.equals(initial) && !status.equals("CANCELLED")
                && !isReleaseStatus(status, strategy);
    }

    private boolean isReleaseStatus(String status, PropertyStrategy strategy) {
        List<String> statuses = strategy.getValidStatuses();
        // The second-to-last status is typically the "release" status (CHECKED_OUT,
        // VACATED, DISCHARGED, TERMINATED)
        return statuses.size() >= 3 && status.equals(statuses.get(statuses.size() - 2));
    }

    private PropertyType resolvePropertyType(BookingDTO dto, Room room) {
        if (dto.getPropertyType() != null) {
            return PropertyType.valueOf(dto.getPropertyType().toUpperCase());
        }
        if (room.getProperty() != null) {
            return room.getProperty().getPropertyType();
        }
        return PropertyType.HOTEL; // backward-compatible default
    }

    /**
     * Legacy check-in method — delegates to strategy-based transition.
     */
    @Transactional
    public BookingDTO checkIn(Long id) {
        Booking booking = findBooking(id);
        PropertyStrategy strategy = strategyFactory.getStrategy(
                booking.getPropertyType() != null ? booking.getPropertyType() : PropertyType.HOTEL);
        String targetStatus = strategy.getValidStatuses().get(1);
        return transitionStatus(id, targetStatus);
    }

    /**
     * Legacy check-out method — delegates to strategy-based transition.
     */
    @Transactional
    public BookingDTO checkOut(Long id) {
        Booking booking = findBooking(id);
        PropertyStrategy strategy = strategyFactory.getStrategy(
                booking.getPropertyType() != null ? booking.getPropertyType() : PropertyType.HOTEL);
        List<String> statuses = strategy.getValidStatuses();
        String targetStatus = statuses.get(statuses.size() - 2);
        return transitionStatus(id, targetStatus);
    }

    /**
     * Legacy cancel method — delegates to strategy-based transition.
     */
    @Transactional
    public BookingDTO cancelBooking(Long id) {
        return transitionStatus(id, "CANCELLED");
    }

    public long getOccupancyCount() {
        return bookingRepository.countCheckedIn();
    }

    public List<BookingDTO> getTodaysCheckIns() {
        return bookingRepository.findTodaysCheckIns(LocalDate.now()).stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getTodaysCheckOuts() {
        return bookingRepository.findTodaysCheckOuts(LocalDate.now()).stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    private BookingDTO toDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        dto.setBookingNumber(b.getBookingNumber());
        dto.setGuestId(b.getGuest().getId());
        dto.setGuestName(b.getGuest().getFirstName() + " " + b.getGuest().getLastName());
        dto.setRoomId(b.getRoom().getId());
        dto.setRoomNumber(b.getRoom().getRoomNumber());
        dto.setRoomTypeName(b.getRoom().getRoomType().getName());
        dto.setPropertyId(b.getProperty() != null ? b.getProperty().getId() : null);
        dto.setPropertyName(b.getProperty() != null ? b.getProperty().getName() : null);
        dto.setPropertyType(b.getPropertyType() != null ? b.getPropertyType().name() : "HOTEL");
        dto.setCheckInDate(b.getCheckInDate());
        dto.setCheckOutDate(b.getCheckOutDate());
        dto.setNumGuests(b.getNumGuests());
        dto.setStatus(b.getStatus());
        dto.setSpecialRequests(b.getSpecialRequests());
        dto.setTotalAmount(b.getTotalAmount());
        dto.setActualCheckIn(b.getActualCheckIn() != null ? b.getActualCheckIn().toString() : null);
        dto.setActualCheckOut(b.getActualCheckOut() != null ? b.getActualCheckOut().toString() : null);
        return dto;
    }
}
