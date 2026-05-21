package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false, unique = true)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 5;

    @Column(length = 20)
    private String unit = "pcs";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
