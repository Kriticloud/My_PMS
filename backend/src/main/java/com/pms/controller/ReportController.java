package com.pms.controller;

import com.pms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK', 'RESTAURANT_STAFF')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.getDailyRevenueReport(days));
    }

    @GetMapping("/revenue/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueSummary(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.getRevenueSummary(days));
    }

    @GetMapping("/occupancy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOccupancyReport() {
        return ResponseEntity.ok(reportService.getOccupancyReport());
    }

    @GetMapping("/pos-sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPosSalesReport(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.getPosSalesReport(days));
    }

    @GetMapping("/pos-sales/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPosSalesSummary(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.getPosSalesSummary(days));
    }
}
