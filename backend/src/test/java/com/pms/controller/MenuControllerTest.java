package com.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.dto.MenuItemDTO;
import com.pms.entity.MenuCategory;
import com.pms.security.JwtTokenProvider;
import com.pms.security.CustomUserDetailsService;
import com.pms.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import com.pms.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import com.pms.config.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@Import(TestSecurityConfig.class)
@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuService menuService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private MenuItemDTO sampleItem() {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(1L);
        dto.setName("Coffee");
        dto.setCategoryId(1L);
        dto.setCategoryName("Beverages");
        dto.setPrice(new BigDecimal("150"));
        dto.setIsAvailable(true);
        dto.setStockQuantity(50);
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategories_returns200() throws Exception {
        MenuCategory cat = MenuCategory.builder().id(1L).name("Beverages").build();
        when(menuService.getAllCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/menu/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Beverages"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void getAvailableItems_returns200() throws Exception {
        when(menuService.getAvailableItems()).thenReturn(List.of(sampleItem()));

        mockMvc.perform(get("/api/menu/items/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Coffee"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMenuItem_valid_returns201() throws Exception {
        MenuItemDTO dto = sampleItem();
        dto.setId(null);
        when(menuService.createMenuItem(any(MenuItemDTO.class))).thenReturn(sampleItem());

        mockMvc.perform(post("/api/menu/items").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Coffee"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMenuItem_missingName_returns400() throws Exception {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("100"));

        mockMvc.perform(post("/api/menu/items").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void createMenuItem_forbiddenForStaff() throws Exception {
        MenuItemDTO dto = sampleItem();
        mockMvc.perform(post("/api/menu/items").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getItemsByCategory_returns200() throws Exception {
        when(menuService.getItemsByCategory(1L)).thenReturn(List.of(sampleItem()));

        mockMvc.perform(get("/api/menu/items/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMenuItem_returns200() throws Exception {
        MenuItemDTO dto = sampleItem();
        when(menuService.updateMenuItem(eq(1L), any(MenuItemDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/menu/items/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coffee"));
    }
}
