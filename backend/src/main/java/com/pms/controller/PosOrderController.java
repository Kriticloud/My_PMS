package com.pms.controller;

import com.pms.dto.PosOrderDTO;
import com.pms.service.PosOrderService;
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
@RequestMapping("/api/pos/orders")
@RequiredArgsConstructor
public class PosOrderController {

    private final PosOrderService posOrderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<List<PosOrderDTO>> getAllOrders() {
        return ResponseEntity.ok(posOrderService.getAllOrders());
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<Page<PosOrderDTO>> getOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(posOrderService.getOrdersPaged(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<PosOrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(posOrderService.getOrderById(id));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF', 'FRONT_DESK')")
    public ResponseEntity<List<PosOrderDTO>> getOrdersByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(posOrderService.getOrdersByBooking(bookingId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<PosOrderDTO> createOrder(@Valid @RequestBody PosOrderDTO dto) {
        return ResponseEntity.ok(posOrderService.createOrder(dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<PosOrderDTO> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(posOrderService.updateOrderStatus(id, status));
    }
}
