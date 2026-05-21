package com.pms.service;

import com.pms.dto.PosOrderDTO;
import com.pms.dto.PosOrderItemDTO;
import com.pms.entity.*;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PosOrderService {

    private final PosOrderRepository posOrderRepository;
    private final BookingRepository bookingRepository;
    private final MenuItemRepository menuItemRepository;
    private final InventoryRepository inventoryRepository;

    public List<PosOrderDTO> getAllOrders() {
        return posOrderRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Page<PosOrderDTO> getOrdersPaged(Pageable pageable) {
        return posOrderRepository.findAll(pageable).map(this::toDTO);
    }

    public PosOrderDTO getOrderById(Long id) {
        return toDTO(findOrder(id));
    }

    public List<PosOrderDTO> getOrdersByBooking(Long bookingId) {
        return posOrderRepository.findByBookingId(bookingId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public PosOrderDTO createOrder(PosOrderDTO dto) {
        PosOrder order = PosOrder.builder()
                .orderNumber("POS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderType(dto.getOrderType())
                .guestName(dto.getGuestName())
                .roomNumber(dto.getRoomNumber())
                .status("PENDING")
                .items(new ArrayList<>())
                .build();

        if (dto.getBookingId() != null) {
            Booking booking = bookingRepository.findById(dto.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            order.setBooking(booking);
            order.setGuestName(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName());
            order.setRoomNumber(booking.getRoom().getRoomNumber());
        } else if ("ROOM_SERVICE".equals(dto.getOrderType()) && dto.getRoomNumber() != null) {
            bookingRepository.findActiveBookingByRoomNumber(dto.getRoomNumber())
                    .ifPresent(booking -> {
                        order.setBooking(booking);
                        order.setGuestName(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName());
                    });
        }

        BigDecimal total = BigDecimal.ZERO;
        for (PosOrderItemDTO itemDto : dto.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemDto.getMenuItemId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Menu item not found: " + itemDto.getMenuItemId()));

            Inventory inventory = inventoryRepository.findByMenuItemId(menuItem.getId()).orElse(null);
            if (inventory != null) {
                if (inventory.getQuantity() < itemDto.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for: " + menuItem.getName());
                }
                inventory.setQuantity(inventory.getQuantity() - itemDto.getQuantity());
                inventoryRepository.save(inventory);
            }

            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            PosOrderItem orderItem = PosOrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .notes(itemDto.getNotes())
                    .build();
            order.getItems().add(orderItem);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        return toDTO(posOrderRepository.save(order));
    }

    @Transactional
    public PosOrderDTO updateOrderStatus(Long id, String status) {
        PosOrder order = findOrder(id);
        order.setStatus(status);
        return toDTO(posOrderRepository.save(order));
    }

    public BigDecimal getDailyRevenue() {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return posOrderRepository.getDailyRevenue(start, end);
    }

    private PosOrder findOrder(Long id) {
        return posOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("POS order not found with id: " + id));
    }

    private PosOrderDTO toDTO(PosOrder o) {
        PosOrderDTO dto = new PosOrderDTO();
        dto.setId(o.getId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setBookingId(o.getBooking() != null ? o.getBooking().getId() : null);
        dto.setGuestName(o.getGuestName());
        dto.setRoomNumber(o.getRoomNumber());
        dto.setOrderType(o.getOrderType());
        dto.setStatus(o.getStatus());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setCreatedAt(o.getCreatedAt().toString());
        dto.setItems(o.getItems().stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    private PosOrderItemDTO toItemDTO(PosOrderItem item) {
        PosOrderItemDTO dto = new PosOrderItemDTO();
        dto.setId(item.getId());
        dto.setMenuItemId(item.getMenuItem().getId());
        dto.setMenuItemName(item.getMenuItem().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setNotes(item.getNotes());
        return dto;
    }
}
