package com.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.dto.RoomDTO;
import com.pms.entity.RoomType;
import com.pms.exception.ResourceNotFoundException;
import com.pms.security.JwtTokenProvider;
import com.pms.security.CustomUserDetailsService;
import com.pms.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
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

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private RoomDTO sampleRoom() {
        RoomDTO dto = new RoomDTO();
        dto.setId(1L);
        dto.setRoomNumber("101");
        dto.setRoomTypeId(1L);
        dto.setRoomTypeName("Deluxe");
        dto.setBasePrice(new BigDecimal("3000"));
        dto.setFloor(1);
        dto.setStatus("AVAILABLE");
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRooms_returnsRoomList() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of(sampleRoom()));

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roomNumber").value("101"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoomById_exists_returnsRoom() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(sampleRoom());

        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.roomTypeName").value("Deluxe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoomById_notFound_returns404() throws Exception {
        when(roomService.getRoomById(99L)).thenThrow(new ResourceNotFoundException("Room not found"));

        mockMvc.perform(get("/api/rooms/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoom_valid_returns201() throws Exception {
        RoomDTO dto = sampleRoom();
        dto.setId(null);
        when(roomService.createRoom(any(RoomDTO.class))).thenReturn(sampleRoom());

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("101"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoom_missingRoomNumber_returns400() throws Exception {
        RoomDTO dto = new RoomDTO();
        dto.setRoomTypeId(1L);
        dto.setFloor(1);
        // roomNumber missing (blank)

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoomStatus_returns200() throws Exception {
        RoomDTO updated = sampleRoom();
        updated.setStatus("MAINTENANCE");
        when(roomService.updateRoomStatus(eq(1L), eq("MAINTENANCE"))).thenReturn(updated);

        mockMvc.perform(patch("/api/rooms/1/status")
                .param("status", "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoom_returns204() throws Exception {
        doNothing().when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void getAllRooms_forbiddenForRestaurantStaff() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRooms_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getRoomTypes_frontDeskAllowed() throws Exception {
        RoomType rt = RoomType.builder().id(1L).name("Deluxe").basePrice(new BigDecimal("3000")).build();
        when(roomService.getAllRoomTypes()).thenReturn(List.of(rt));

        mockMvc.perform(get("/api/rooms/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void createRoom_frontDeskForbidden() throws Exception {
        RoomDTO dto = sampleRoom();
        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
