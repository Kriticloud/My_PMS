package com.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.dto.PosOrderDTO;
import com.pms.dto.PosOrderItemDTO;
import com.pms.security.JwtTokenProvider;
import com.pms.security.CustomUserDetailsService;
import com.pms.service.PosOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PosOrderController.class)
class PosOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PosOrderService posOrderService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private PosOrderDTO sampleOrder() {
        PosOrderItemDTO item = new PosOrderItemDTO();
        item.setMenuItemId(1L);
        item.setMenuItemName("Coffee");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("150"));
        item.setSubtotal(new BigDecimal("300"));

        PosOrderDTO dto = new PosOrderDTO();
        dto.setId(1L);
        dto.setOrderNumber("POS-TEST0001");
        dto.setOrderType("DINE_IN");
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("300"));
        dto.setItems(List.of(item));
        return dto;
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void getAllOrders_returns200() throws Exception {
        when(posOrderService.getAllOrders()).thenReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/pos/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNumber").value("POS-TEST0001"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void createOrder_valid_returns201() throws Exception {
        PosOrderDTO dto = sampleOrder();
        dto.setId(null);
        when(posOrderService.createOrder(any(PosOrderDTO.class))).thenReturn(sampleOrder());

        mockMvc.perform(post("/api/pos/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("POS-TEST0001"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void createOrder_missingOrderType_returns400() throws Exception {
        PosOrderDTO dto = new PosOrderDTO();
        PosOrderItemDTO item = new PosOrderItemDTO();
        item.setMenuItemId(1L);
        item.setQuantity(1);
        dto.setItems(List.of(item));
        // orderType is null

        mockMvc.perform(post("/api/pos/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void createOrder_emptyItems_returns400() throws Exception {
        PosOrderDTO dto = new PosOrderDTO();
        dto.setOrderType("DINE_IN");
        dto.setItems(List.of());

        mockMvc.perform(post("/api/pos/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void updateOrderStatus_returns200() throws Exception {
        PosOrderDTO dto = sampleOrder();
        dto.setStatus("COMPLETED");
        when(posOrderService.updateOrderStatus(eq(1L), eq("COMPLETED"))).thenReturn(dto);

        mockMvc.perform(patch("/api/pos/orders/1/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getAllOrders_forbiddenForFrontDesk() throws Exception {
        mockMvc.perform(get("/api/pos/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getOrdersByBooking_allowedForFrontDesk() throws Exception {
        when(posOrderService.getOrdersByBooking(1L)).thenReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/pos/orders/booking/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void getOrderById_returns200() throws Exception {
        when(posOrderService.getOrderById(1L)).thenReturn(sampleOrder());

        mockMvc.perform(get("/api/pos/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("POS-TEST0001"));
    }
}
