package com.pms.service;

import com.pms.dto.BookingDTO;
import com.pms.entity.Booking;
import com.pms.entity.Guest;
import com.pms.entity.Room;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.BookingRepository;
import com.pms.repository.GuestRepository;
import com.pms.repository.RoomRepository;
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

        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate())
                || dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                room.getId(), dto.getCheckInDate(), dto.getCheckOutDate());
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Room is not available for the selected dates");
        }

        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal total = room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(nights));

        Booking booking = Booking.builder()
                .bookingNumber("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .guest(guest)
                .room(room)
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .numGuests(dto.getNumGuests() != null ? dto.getNumGuests() : 1)
                .status("RESERVED")
                .specialRequests(dto.getSpecialRequests())
                .totalAmount(total)
                .build();

        return toDTO(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO checkIn(Long id) {
        Booking booking = findBooking(id);
        if (!"RESERVED".equals(booking.getStatus())) {
            throw new BadRequestException("Only RESERVED bookings can be checked in");
        }
        booking.setStatus("CHECKED_IN");
        booking.setActualCheckIn(LocalDateTime.now());
        booking.getRoom().setStatus("OCCUPIED");
        roomRepository.save(booking.getRoom());
        return toDTO(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO checkOut(Long id) {
        Booking booking = findBooking(id);
        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new BadRequestException("Only CHECKED_IN bookings can be checked out");
        }
        booking.setStatus("CHECKED_OUT");
        booking.setActualCheckOut(LocalDateTime.now());
        booking.getRoom().setStatus("CLEANING");
        roomRepository.save(booking.getRoom());
        return toDTO(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDTO cancelBooking(Long id) {
        Booking booking = findBooking(id);
        if ("CHECKED_IN".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
            throw new BadRequestException("Cannot cancel a checked-in or checked-out booking");
        }
        booking.setStatus("CANCELLED");
        return toDTO(bookingRepository.save(booking));
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
