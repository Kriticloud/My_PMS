package com.pms.service;

import com.pms.dto.InvoiceDTO;
import com.pms.entity.*;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PosOrderRepository posOrderRepository;
    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Guest guest;
    private Room room;
    private RoomType roomType;
    private Booking booking;

    @BeforeEach
    void setUp() {
        guest = Guest.builder().id(1L).firstName("John").lastName("Doe").build();
        roomType = RoomType.builder().id(1L).name("Deluxe").basePrice(new BigDecimal("3000")).build();
        room = Room.builder().id(1L).roomNumber("101").roomType(roomType).build();
        booking = Booking.builder()
                .id(1L)
                .bookingNumber("BK-TEST0001")
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(3))
                .totalAmount(new BigDecimal("9000"))
                .status("CHECKED_OUT")
                .build();
    }

    @Test
    void generateInvoice_roomOnly_calculatesCorrectTotals() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(posOrderRepository.findByBookingId(1L)).thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice saved = inv.getArgument(0);
            saved.setId(1L);
            saved.setGeneratedAt(LocalDateTime.now());
            return saved;
        });

        InvoiceDTO result = invoiceService.generateInvoice(1L, null);

        assertThat(result.getSubtotal()).isEqualByComparingTo("9000");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("1620"); // 9000 * 0.18
        assertThat(result.getTotalAmount()).isEqualByComparingTo("10620");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemType()).isEqualTo("ROOM_CHARGE");
    }

    @Test
    void generateInvoice_withPosOrders_includesPosCharges() {
        PosOrder posOrder = PosOrder.builder()
                .id(1L)
                .orderNumber("POS-001")
                .orderType("DINE_IN")
                .status("COMPLETED")
                .totalAmount(new BigDecimal("1000"))
                .items(new ArrayList<>())
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(posOrderRepository.findByBookingId(1L)).thenReturn(List.of(posOrder));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice saved = inv.getArgument(0);
            saved.setId(1L);
            saved.setGeneratedAt(LocalDateTime.now());
            return saved;
        });

        InvoiceDTO result = invoiceService.generateInvoice(1L, null);

        // subtotal = 9000 + 1000 = 10000, tax = 1800, total = 11800
        assertThat(result.getSubtotal()).isEqualByComparingTo("10000");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("11800");
        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    void generateInvoice_withDiscount_appliesDiscount() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(posOrderRepository.findByBookingId(1L)).thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice saved = inv.getArgument(0);
            saved.setId(1L);
            saved.setGeneratedAt(LocalDateTime.now());
            return saved;
        });

        InvoiceDTO result = invoiceService.generateInvoice(1L, BigDecimal.TEN); // 10% discount

        // subtotal = 9000 - 900 = 8100, tax = 1458, total = 9558
        assertThat(result.getSubtotal()).isEqualByComparingTo("8100");
        assertThat(result.getItems()).hasSize(2); // room + discount
    }

    @Test
    void generateInvoice_duplicateInvoice_throwsBadRequest() {
        Invoice existing = Invoice.builder().id(1L).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> invoiceService.generateInvoice(1L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void generateInvoice_bookingNotFound_throwsNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.generateInvoice(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateInvoice_cancelledPosOrders_excluded() {
        PosOrder cancelled = PosOrder.builder()
                .id(2L)
                .orderNumber("POS-002")
                .orderType("ROOM_SERVICE")
                .status("CANCELLED")
                .totalAmount(new BigDecimal("500"))
                .items(new ArrayList<>())
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(posOrderRepository.findByBookingId(1L)).thenReturn(List.of(cancelled));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice saved = inv.getArgument(0);
            saved.setId(1L);
            saved.setGeneratedAt(LocalDateTime.now());
            return saved;
        });

        InvoiceDTO result = invoiceService.generateInvoice(1L, null);

        assertThat(result.getSubtotal()).isEqualByComparingTo("9000"); // no POS added
        assertThat(result.getItems()).hasSize(1); // only room charge
    }

    @Test
    void getInvoiceById_notFound_throwsNotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoiceById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
