package com.pms.repository;

import com.pms.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByBookingId(Long bookingId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
            "WHERE i.status = 'PAID' AND i.generatedAt BETWEEN :start AND :end")
    java.math.BigDecimal getDailyRevenue(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
