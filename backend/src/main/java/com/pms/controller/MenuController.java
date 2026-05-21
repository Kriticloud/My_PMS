package com.pms.controller;

import com.pms.dto.MenuItemDTO;
import com.pms.entity.MenuCategory;
import com.pms.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<List<MenuCategory>> getCategories() {
        return ResponseEntity.ok(menuService.getAllCategories());
    }

    @GetMapping("/items")
    public ResponseEntity<List<MenuItemDTO>> getAllItems() {
        return ResponseEntity.ok(menuService.getAllMenuItems());
    }

    @GetMapping("/items/available")
    public ResponseEntity<List<MenuItemDTO>> getAvailableItems() {
        return ResponseEntity.ok(menuService.getAvailableItems());
    }

    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<MenuItemDTO>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getItemsByCategory(categoryId));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDTO> createItem(@Valid @RequestBody MenuItemDTO dto) {
        return ResponseEntity.ok(menuService.createMenuItem(dto));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDTO> updateItem(@PathVariable Long id, @Valid @RequestBody MenuItemDTO dto) {
        return ResponseEntity.ok(menuService.updateMenuItem(id, dto));
    }
}
