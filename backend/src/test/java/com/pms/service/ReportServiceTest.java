package com.pms.service;

import com.pms.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pms.entity.Room;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private PosOrderRepository posOrderRepository;
    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getDashboardStats_returnsAllExpectedKeys() {
        when(roomRepository.count()).thenReturn(10L);
        when(bookingRepository.countCheckedIn()).thenReturn(6L);
        when(invoiceRepository.getDailyRevenue(any(), any())).thenReturn(new BigDecimal("50000"));
        when(posOrderRepository.getDailyRevenue(any(), any())).thenReturn(new BigDecimal("12000"));
        when(bookingRepository.findTodaysCheckIns(any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findTodaysCheckOuts(any())).thenReturn(Collections.emptyList());
        when(bookingRepository.count()).thenReturn(25L);

        Map<String, Object> stats = reportService.getDashboardStats();

        assertThat(stats).containsKeys(
                "totalRooms", "occupiedRooms", "availableRooms", "occupancyRate",
                "todaysCheckIns", "todaysCheckOuts", "dailyRevenue", "posRevenue", "totalBookings");
        assertThat(stats.get("totalRooms")).isEqualTo(10L);
        assertThat(stats.get("occupiedRooms")).isEqualTo(6L);
        assertThat(stats.get("availableRooms")).isEqualTo(4L);
        assertThat(stats.get("occupancyRate")).isEqualTo(60L);
    }

    @Test
    void getDailyRevenueReport_returnsCorrectNumberOfDays() {
        when(invoiceRepository.getDailyRevenue(any(), any())).thenReturn(BigDecimal.ZERO);
        when(posOrderRepository.getDailyRevenue(any(), any())).thenReturn(BigDecimal.ZERO);

        List<Map<String, Object>> report = reportService.getDailyRevenueReport(7);

        assertThat(report).hasSize(7);
        assertThat(report.get(0)).containsKeys("date", "invoiceRevenue", "posRevenue", "totalRevenue");
    }

    @Test
    void getOccupancyReport_returnsExpectedFields() {
        Room r1 = Room.builder().id(1L).roomNumber("101").status("AVAILABLE").build();
        Room r2 = Room.builder().id(2L).roomNumber("102").status("OCCUPIED").build();
        Room r3 = Room.builder().id(3L).roomNumber("103").status("CLEANING").build();

        when(roomRepository.count()).thenReturn(3L);
        when(roomRepository.findAll()).thenReturn(List.of(r1, r2, r3));

        Map<String, Object> report = reportService.getOccupancyReport();

        assertThat(report.get("totalRooms")).isEqualTo(3L);
        assertThat(report.get("occupiedRooms")).isEqualTo(1L);
        assertThat(report.get("availableRooms")).isEqualTo(1L);
        assertThat(report.get("cleaningRooms")).isEqualTo(1L);
        assertThat(report.get("maintenanceRooms")).isEqualTo(0L);
        assertThat(report).containsKey("statusBreakdown");
    }

    @Test
    void getRevenueSummary_includesDailyBreakdown() {
        when(invoiceRepository.getDailyRevenue(any(), any())).thenReturn(new BigDecimal("10000"));
        when(posOrderRepository.getDailyRevenue(any(), any())).thenReturn(new BigDecimal("5000"));

        Map<String, Object> summary = reportService.getRevenueSummary(7);

        assertThat(summary).containsKeys("invoiceRevenue", "posRevenue", "totalRevenue", "dailyBreakdown");
        assertThat(summary.get("totalRevenue")).isEqualTo(new BigDecimal("10000").add(new BigDecimal("5000")));
    }

    @Test
    void getPosSalesSummary_includesSummaryAndBreakdown() {
        when(posOrderRepository.findOrdersBetween(any(), any())).thenReturn(Collections.emptyList());
        when(posOrderRepository.getDailyRevenue(any(), any())).thenReturn(BigDecimal.ZERO);

        Map<String, Object> summary = reportService.getPosSalesSummary(7);

        assertThat(summary).containsKeys("totalOrders", "revenue", "dailyBreakdown");
    }
}
