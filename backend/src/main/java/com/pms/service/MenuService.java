package com.pms.service;

import com.pms.dto.MenuItemDTO;
import com.pms.entity.Inventory;
import com.pms.entity.MenuCategory;
import com.pms.entity.MenuItem;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.InventoryRepository;
import com.pms.repository.MenuCategoryRepository;
import com.pms.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final InventoryRepository inventoryRepository;

    public List<MenuCategory> getAllCategories() {
        return menuCategoryRepository.findAll();
    }

    public List<MenuItemDTO> getAllMenuItems() {
        return menuItemRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MenuItemDTO> getAvailableItems() {
        return menuItemRepository.findByIsAvailableTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MenuItemDTO> getItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryId(categoryId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public MenuItemDTO createMenuItem(MenuItemDTO dto) {
        MenuCategory category = menuCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .category(category)
                .price(dto.getPrice())
                .description(dto.getDescription())
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .build();
        item = menuItemRepository.save(item);

        Inventory inventory = Inventory.builder()
                .menuItem(item)
                .quantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 100)
                .minStockLevel(5)
                .unit("pcs")
                .build();
        inventoryRepository.save(inventory);

        return toDTO(item);
    }

    @Transactional
    public MenuItemDTO updateMenuItem(Long id, MenuItemDTO dto) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (dto.getName() != null)
            item.setName(dto.getName());
        if (dto.getPrice() != null)
            item.setPrice(dto.getPrice());
        if (dto.getDescription() != null)
            item.setDescription(dto.getDescription());
        if (dto.getIsAvailable() != null)
            item.setIsAvailable(dto.getIsAvailable());
        if (dto.getCategoryId() != null) {
            MenuCategory category = menuCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(category);
        }
        return toDTO(menuItemRepository.save(item));
    }

    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    private MenuItemDTO toDTO(MenuItem item) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setCategoryId(item.getCategory().getId());
        dto.setCategoryName(item.getCategory().getName());
        dto.setPrice(item.getPrice());
        dto.setDescription(item.getDescription());
        dto.setIsAvailable(item.getIsAvailable());
        inventoryRepository.findByMenuItemId(item.getId())
                .ifPresent(inv -> dto.setStockQuantity(inv.getQuantity()));
        return dto;
    }
}
