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
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(reportService.getDailyRevenueReport(days));
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
}
