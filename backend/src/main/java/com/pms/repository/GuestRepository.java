package com.pms.repository;

import com.pms.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByLastNameContainingIgnoreCase(String lastName);

    @Query("SELECT g FROM Guest g WHERE LOWER(g.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(g.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Guest> searchByName(String name);
}
