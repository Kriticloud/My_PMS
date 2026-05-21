package com.pms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyRoomStatusChange(Long roomId, String roomNumber, String newStatus) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "ROOM_STATUS");
        payload.put("roomId", roomId);
        payload.put("roomNumber", roomNumber);
        payload.put("status", newStatus);
        payload.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/rooms", payload);
    }

    public void notifyNewOrder(Long orderId, String orderNumber, String orderType) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "NEW_ORDER");
        payload.put("orderId", orderId);
        payload.put("orderNumber", orderNumber);
        payload.put("orderType", orderType);
        payload.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/orders", payload);
    }

    public void notifyBookingUpdate(Long bookingId, String bookingNumber, String status) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "BOOKING_UPDATE");
        payload.put("bookingId", bookingId);
        payload.put("bookingNumber", bookingNumber);
        payload.put("status", status);
        payload.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/bookings", payload);
    }

    public void notifyDashboardUpdate() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "DASHBOARD_REFRESH");
        payload.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/dashboard", payload);
    }
}
