package com.pms.repository;

import com.pms.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByStatus(String status);

    // Check for overlapping bookings on a room
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT', 'VACATED', 'DISCHARGED', 'TERMINATED') " +
            "AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    // Active bookings (checked-in) for a room
    @Query("SELECT b FROM Booking b WHERE b.room.roomNumber = :roomNumber AND b.status = 'CHECKED_IN'")
    Optional<Booking> findActiveBookingByRoomNumber(@Param("roomNumber") String roomNumber);

    // Today's check-ins
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :date AND b.status IN ('RESERVED', 'BOOKED')")
    List<Booking> findTodaysCheckIns(@Param("date") LocalDate date);

    // Today's check-outs
    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :date AND b.status = 'CHECKED_IN'")
    List<Booking> findTodaysCheckOuts(@Param("date") LocalDate date);

    // Occupancy count
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CHECKED_IN'")
    long countCheckedIn();
}
