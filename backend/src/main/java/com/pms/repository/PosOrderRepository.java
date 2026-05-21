package com.pms.repository;

import com.pms.entity.PosOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PosOrderRepository extends JpaRepository<PosOrder, Long> {

    Optional<PosOrder> findByOrderNumber(String orderNumber);

    List<PosOrder> findByBookingId(Long bookingId);

    List<PosOrder> findByStatus(String status);

    @Query("SELECT o FROM PosOrder o WHERE o.createdAt BETWEEN :start AND :end")
    List<PosOrder> findOrdersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Daily POS revenue
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM PosOrder o " +
            "WHERE o.status != 'CANCELLED' AND o.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal getDailyRevenue(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
