package com.pms.repository;

import com.pms.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByMenuItemId(Long menuItemId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStockLevel")
    List<Inventory> findLowStockItems();
}
