package com.pms.repository;

import com.pms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);

    List<Room> findByStatus(String status);

    List<Room> findByRoomTypeId(Long roomTypeId);

    boolean existsByRoomNumber(String roomNumber);
}
