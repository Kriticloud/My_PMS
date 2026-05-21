package com.pms.service;

import com.pms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PosOrderRepository posOrderRepository;
    private final InvoiceRepository invoiceRepository;

    @Cacheable("dashboardStats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalRooms = roomRepository.count();
        long occupiedRooms = bookingRepository.countCheckedIn();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        BigDecimal dailyRevenue = invoiceRepository.getDailyRevenue(todayStart, todayEnd);
        BigDecimal posRevenue = posOrderRepository.getDailyRevenue(todayStart, todayEnd);

        stats.put("totalRooms", totalRooms);
        stats.put("occupiedRooms", occupiedRooms);
        stats.put("availableRooms", totalRooms - occupiedRooms);
        stats.put("occupancyRate", totalRooms > 0 ? Math.round((double) occupiedRooms / totalRooms * 100) : 0);
        stats.put("todaysCheckIns", bookingRepository.findTodaysCheckIns(LocalDate.now()).size());
        stats.put("todaysCheckOuts", bookingRepository.findTodaysCheckOuts(LocalDate.now()).size());
        stats.put("dailyRevenue", dailyRevenue);
        stats.put("posRevenue", posRevenue);
        stats.put("totalBookings", bookingRepository.count());

        return stats;
    }

    public Map<String, Object> getRevenueSummary(int days) {
        Map<String, Object> summary = new LinkedHashMap<>();
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        BigDecimal invoiceRevenue = invoiceRepository.getDailyRevenue(start, end);
        BigDecimal posRevenue = posOrderRepository.getDailyRevenue(start, end);

        summary.put("invoiceRevenue", invoiceRevenue);
        summary.put("posRevenue", posRevenue);
        summary.put("totalRevenue", invoiceRevenue.add(posRevenue));
        summary.put("dailyBreakdown", getDailyRevenueReport(days));
        return summary;
    }

    public List<Map<String, Object>> getDailyRevenueReport(int days) {
        List<Map<String, Object>> report = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            BigDecimal invoiceRevenue = invoiceRepository.getDailyRevenue(start, end);
            BigDecimal posRevenue = posOrderRepository.getDailyRevenue(start, end);

            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.toString());
            day.put("invoiceRevenue", invoiceRevenue);
            day.put("posRevenue", posRevenue);
            day.put("totalRevenue", invoiceRevenue.add(posRevenue));
            report.add(day);
        }
        return report;
    }

    public Map<String, Object> getOccupancyReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        long totalRooms = roomRepository.count();

        // Count by status
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        roomRepository.findAll().forEach(room -> statusBreakdown.merge(room.getStatus(), 1L, Long::sum));

        long occupied = statusBreakdown.getOrDefault("OCCUPIED", 0L);
        long available = statusBreakdown.getOrDefault("AVAILABLE", 0L);
        long cleaning = statusBreakdown.getOrDefault("CLEANING", 0L);
        long maintenance = statusBreakdown.getOrDefault("MAINTENANCE", 0L);

        report.put("totalRooms", totalRooms);
        report.put("occupiedRooms", occupied);
        report.put("availableRooms", available);
        report.put("cleaningRooms", cleaning);
        report.put("maintenanceRooms", maintenance);
        report.put("occupancyRate", totalRooms > 0 ? Math.round((double) occupied / totalRooms * 100) : 0);
        report.put("statusBreakdown", statusBreakdown);

        return report;
    }

    public Map<String, Object> getPosSalesSummary(int days) {
        Map<String, Object> summary = new LinkedHashMap<>();
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        List<com.pms.entity.PosOrder> orders = posOrderRepository.findOrdersBetween(start, end);
        long totalOrders = orders.stream().filter(o -> !"CANCELLED".equals(o.getStatus())).count();
        BigDecimal revenue = posOrderRepository.getDailyRevenue(start, end);

        summary.put("totalOrders", totalOrders);
        summary.put("revenue", revenue);
        summary.put("dailyBreakdown", getPosSalesReport(days));
        return summary;
    }

    public List<Map<String, Object>> getPosSalesReport(int days) {
        List<Map<String, Object>> report = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            List<com.pms.entity.PosOrder> orders = posOrderRepository.findOrdersBetween(start, end);

            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.toString());
            day.put("totalOrders", orders.size());
            day.put("totalRevenue", posOrderRepository.getDailyRevenue(start, end));
            report.add(day);
        }
        return report;
    }
}
