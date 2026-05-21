package com.pms.service;

import com.pms.dto.BookingDTO;
import com.pms.entity.*;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.BookingRepository;
import com.pms.repository.GuestRepository;
import com.pms.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private WebSocketNotificationService wsNotificationService;

    @InjectMocks
    private BookingService bookingService;

    private Guest guest;
    private RoomType roomType;
    private Room room;
    private Booking booking;

    @BeforeEach
    void setUp() {
        guest = Guest.builder().id(1L).firstName("John").lastName("Doe").build();
        roomType = RoomType.builder().id(1L).name("Deluxe").basePrice(new BigDecimal("3000")).build();
        room = Room.builder().id(1L).roomNumber("101").roomType(roomType).floor(1).status("AVAILABLE").build();
        booking = Booking.builder()
                .id(1L)
                .bookingNumber("BK-TEST0001")
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(3))
                .numGuests(2)
                .status("RESERVED")
                .totalAmount(new BigDecimal("9000"))
                .build();
    }

    @Test
    void createBooking_validDates_calculatesTotal() {
        BookingDTO dto = new BookingDTO();
        dto.setGuestId(1L);
        dto.setRoomId(1L);
        dto.setCheckInDate(LocalDate.now().plusDays(1));
        dto.setCheckOutDate(LocalDate.now().plusDays(4)); // 3 nights

        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        BookingDTO result = bookingService.createBooking(dto);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("9000"); // 3000 * 3 nights
        assertThat(result.getStatus()).isEqualTo("RESERVED");
    }

    @Test
    void createBooking_checkOutBeforeCheckIn_throwsBadRequest() {
        BookingDTO dto = new BookingDTO();
        dto.setGuestId(1L);
        dto.setRoomId(1L);
        dto.setCheckInDate(LocalDate.now().plusDays(5));
        dto.setCheckOutDate(LocalDate.now().plusDays(2));

        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.createBooking(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Check-out date must be after");
    }

    @Test
    void createBooking_overlappingDates_throwsBadRequest() {
        BookingDTO dto = new BookingDTO();
        dto.setGuestId(1L);
        dto.setRoomId(1L);
        dto.setCheckInDate(LocalDate.now());
        dto.setCheckOutDate(LocalDate.now().plusDays(3));

        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of(booking));

        assertThatThrownBy(() -> bookingService.createBooking(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void createBooking_guestNotFound_throwsNotFound() {
        BookingDTO dto = new BookingDTO();
        dto.setGuestId(99L);
        dto.setRoomId(1L);
        dto.setCheckInDate(LocalDate.now());
        dto.setCheckOutDate(LocalDate.now().plusDays(1));

        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void checkIn_reservedBooking_setsCheckedInAndOccupied() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        BookingDTO result = bookingService.checkIn(1L);

        assertThat(booking.getStatus()).isEqualTo("CHECKED_IN");
        assertThat(booking.getActualCheckIn()).isNotNull();
        assertThat(room.getStatus()).isEqualTo("OCCUPIED");
        verify(wsNotificationService).notifyRoomStatusChange(anyLong(), anyString(), eq("OCCUPIED"));
    }

    @Test
    void checkIn_nonReservedBooking_throwsBadRequest() {
        booking.setStatus("CHECKED_IN");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.checkIn(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RESERVED");
    }

    @Test
    void checkOut_checkedInBooking_setsCheckedOutAndCleaning() {
        booking.setStatus("CHECKED_IN");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        BookingDTO result = bookingService.checkOut(1L);

        assertThat(booking.getStatus()).isEqualTo("CHECKED_OUT");
        assertThat(booking.getActualCheckOut()).isNotNull();
        assertThat(room.getStatus()).isEqualTo("CLEANING");
    }

    @Test
    void cancelBooking_reservedBooking_setsCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDTO result = bookingService.cancelBooking(1L);

        assertThat(booking.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelBooking_checkedInBooking_throwsBadRequest() {
        booking.setStatus("CHECKED_IN");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getAllBookings_returnsMappedDTOs() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<BookingDTO> result = bookingService.getAllBookings();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookingNumber()).isEqualTo("BK-TEST0001");
        assertThat(result.get(0).getGuestName()).isEqualTo("John Doe");
    }
}
