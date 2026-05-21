package com.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.dto.GuestDTO;
import com.pms.exception.ResourceNotFoundException;
import com.pms.security.JwtTokenProvider;
import com.pms.security.CustomUserDetailsService;
import com.pms.service.GuestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import com.pms.config.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(GuestController.class)
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GuestService guestService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private GuestDTO sampleGuest() {
        GuestDTO dto = new GuestDTO();
        dto.setId(1L);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPhone("9876543210");
        return dto;
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getAllGuests_returns200() throws Exception {
        when(guestService.getAllGuests()).thenReturn(List.of(sampleGuest()));

        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void createGuest_valid_returns201() throws Exception {
        GuestDTO dto = sampleGuest();
        dto.setId(null);
        when(guestService.createGuest(any(GuestDTO.class))).thenReturn(sampleGuest());

        mockMvc.perform(post("/api/guests").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void createGuest_missingFirstName_returns400() throws Exception {
        GuestDTO dto = new GuestDTO();
        dto.setLastName("Doe");

        mockMvc.perform(post("/api/guests").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void searchGuests_returns200() throws Exception {
        when(guestService.searchGuests("doe")).thenReturn(List.of(sampleGuest()));

        mockMvc.perform(get("/api/guests/search").param("name", "doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGuest_returns204() throws Exception {
        doNothing().when(guestService).deleteGuest(1L);

        mockMvc.perform(delete("/api/guests/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void deleteGuest_frontDeskForbidden() throws Exception {
        mockMvc.perform(delete("/api/guests/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getGuestById_notFound_returns404() throws Exception {
        when(guestService.getGuestById(99L)).thenThrow(new ResourceNotFoundException("Guest not found"));

        mockMvc.perform(get("/api/guests/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void updateGuest_returns200() throws Exception {
        GuestDTO dto = sampleGuest();
        when(guestService.updateGuest(eq(1L), any(GuestDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/guests/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }
}
