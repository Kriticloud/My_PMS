package com.pms.controller;

import com.pms.dto.BookingDTO;
import com.pms.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<Page<BookingDTO>> getBookingsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(bookingService.getBookingsPaged(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<List<BookingDTO>> getBookingsByGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(bookingService.getBookingsByGuest(guestId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingDTO dto) {
        return ResponseEntity.ok(bookingService.createBooking(dto));
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<BookingDTO> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkIn(id));
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<BookingDTO> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkOut(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PostMapping("/{id}/transition")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<BookingDTO> transitionStatus(
            @PathVariable Long id,
            @RequestParam String targetStatus) {
        return ResponseEntity.ok(bookingService.transitionStatus(id, targetStatus));
    }
}
