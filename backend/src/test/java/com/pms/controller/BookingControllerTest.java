package com.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.dto.BookingDTO;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.security.JwtTokenProvider;
import com.pms.security.CustomUserDetailsService;
import com.pms.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import com.pms.config.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private BookingDTO sampleBooking() {
        BookingDTO dto = new BookingDTO();
        dto.setId(1L);
        dto.setBookingNumber("BK-TEST0001");
        dto.setGuestId(1L);
        dto.setGuestName("John Doe");
        dto.setRoomId(1L);
        dto.setRoomNumber("101");
        dto.setRoomTypeName("Deluxe");
        dto.setCheckInDate(LocalDate.now().plusDays(1));
        dto.setCheckOutDate(LocalDate.now().plusDays(4));
        dto.setNumGuests(2);
        dto.setStatus("RESERVED");
        dto.setTotalAmount(new BigDecimal("9000"));
        return dto;
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getAllBookings_returns200() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of(sampleBooking()));

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookingNumber").value("BK-TEST0001"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void createBooking_valid_returns201() throws Exception {
        BookingDTO dto = sampleBooking();
        dto.setId(null);
        when(bookingService.createBooking(any(BookingDTO.class))).thenReturn(sampleBooking());

        mockMvc.perform(post("/api/bookings").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingNumber").value("BK-TEST0001"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void createBooking_missingGuestId_returns400() throws Exception {
        BookingDTO dto = new BookingDTO();
        dto.setRoomId(1L);
        dto.setCheckInDate(LocalDate.now().plusDays(1));
        dto.setCheckOutDate(LocalDate.now().plusDays(4));
        // guestId is null

        mockMvc.perform(post("/api/bookings").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void createBooking_overlappingDates_returns400() throws Exception {
        BookingDTO dto = sampleBooking();
        when(bookingService.createBooking(any())).thenThrow(new BadRequestException("Room is not available"));

        mockMvc.perform(post("/api/bookings").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void checkIn_reserved_returns200() throws Exception {
        BookingDTO dto = sampleBooking();
        dto.setStatus("CHECKED_IN");
        when(bookingService.checkIn(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/bookings/1/check-in").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void checkIn_notReserved_returns400() throws Exception {
        when(bookingService.checkIn(1L)).thenThrow(new BadRequestException("Only RESERVED bookings"));

        mockMvc.perform(post("/api/bookings/1/check-in").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void checkOut_returns200() throws Exception {
        BookingDTO dto = sampleBooking();
        dto.setStatus("CHECKED_OUT");
        when(bookingService.checkOut(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/bookings/1/check-out").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void cancelBooking_returns200() throws Exception {
        BookingDTO dto = sampleBooking();
        dto.setStatus("CANCELLED");
        when(bookingService.cancelBooking(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/bookings/1/cancel").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void getAllBookings_forbiddenForRestaurantStaff() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getBookingById_notFound_returns404() throws Exception {
        when(bookingService.getBookingById(99L)).thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getBookingsByGuest_returns200() throws Exception {
        when(bookingService.getBookingsByGuest(1L)).thenReturn(List.of(sampleBooking()));

        mockMvc.perform(get("/api/bookings/guest/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
