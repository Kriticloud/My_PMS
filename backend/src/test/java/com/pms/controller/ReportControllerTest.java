package com.pms.controller;

import com.pms.security.JwtTokenProvider;
import com.pms.security.CustomUserDetailsService;
import com.pms.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import com.pms.config.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_returns200() throws Exception {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRooms", 10L);
        stats.put("occupiedRooms", 6L);
        stats.put("availableRooms", 4L);
        stats.put("occupancyRate", 60L);
        when(reportService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/reports/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").value(10))
                .andExpect(jsonPath("$.occupancyRate").value(60));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getDashboardStats_frontDeskAllowed() throws Exception {
        when(reportService.getDashboardStats()).thenReturn(Map.of("totalRooms", 10L));

        mockMvc.perform(get("/api/reports/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void getDashboardStats_restaurantStaffAllowed() throws Exception {
        when(reportService.getDashboardStats()).thenReturn(Map.of("totalRooms", 10L));

        mockMvc.perform(get("/api/reports/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRevenue_returns200() throws Exception {
        Map<String, Object> day = new LinkedHashMap<>();
        day.put("date", "2026-05-20");
        day.put("invoiceRevenue", new BigDecimal("5000"));
        day.put("posRevenue", new BigDecimal("2000"));
        day.put("totalRevenue", new BigDecimal("7000"));
        when(reportService.getDailyRevenueReport(7)).thenReturn(List.of(day));

        mockMvc.perform(get("/api/reports/revenue").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date").value("2026-05-20"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRevenueSummary_returns200() throws Exception {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("invoiceRevenue", new BigDecimal("50000"));
        summary.put("posRevenue", new BigDecimal("20000"));
        summary.put("totalRevenue", new BigDecimal("70000"));
        summary.put("dailyBreakdown", List.of());
        when(reportService.getRevenueSummary(7)).thenReturn(summary);

        mockMvc.perform(get("/api/reports/revenue/summary").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(70000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getOccupancy_returns200() throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalRooms", 10L);
        report.put("occupiedRooms", 5L);
        report.put("availableRooms", 3L);
        report.put("cleaningRooms", 1L);
        report.put("maintenanceRooms", 1L);
        when(reportService.getOccupancyReport()).thenReturn(report);

        mockMvc.perform(get("/api/reports/occupancy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupiedRooms").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPosSalesSummary_returns200() throws Exception {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalOrders", 15L);
        summary.put("revenue", new BigDecimal("25000"));
        summary.put("dailyBreakdown", List.of());
        when(reportService.getPosSalesSummary(7)).thenReturn(summary);

        mockMvc.perform(get("/api/reports/pos-sales/summary").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(15));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getRevenue_forbiddenForFrontDesk() throws Exception {
        mockMvc.perform(get("/api/reports/revenue"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getOccupancy_forbiddenForFrontDesk() throws Exception {
        mockMvc.perform(get("/api/reports/occupancy"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDashboardStats_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
