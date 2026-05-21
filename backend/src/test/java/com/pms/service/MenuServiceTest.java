package com.pms.service;

import com.pms.dto.MenuItemDTO;
import com.pms.entity.Inventory;
import com.pms.entity.MenuCategory;
import com.pms.entity.MenuItem;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.InventoryRepository;
import com.pms.repository.MenuCategoryRepository;
import com.pms.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private MenuCategoryRepository menuCategoryRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private MenuService menuService;

    private MenuCategory category;
    private MenuItem menuItem;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        category = MenuCategory.builder().id(1L).name("Beverages").build();
        menuItem = MenuItem.builder()
                .id(1L)
                .name("Coffee")
                .category(category)
                .price(new BigDecimal("150"))
                .isAvailable(true)
                .build();
        inventory = Inventory.builder()
                .id(1L)
                .menuItem(menuItem)
                .quantity(50)
                .minStockLevel(10)
                .unit("cups")
                .build();
    }

    @Test
    void getAllCategories_returnsList() {
        when(menuCategoryRepository.findAll()).thenReturn(List.of(category));

        List<MenuCategory> result = menuService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Beverages");
    }

    @Test
    void getAllMenuItems_returnsMappedDTOs() {
        when(menuItemRepository.findAll()).thenReturn(List.of(menuItem));
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));

        List<MenuItemDTO> result = menuService.getAllMenuItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Coffee");
        assertThat(result.get(0).getCategoryName()).isEqualTo("Beverages");
        assertThat(result.get(0).getStockQuantity()).isEqualTo(50);
    }

    @Test
    void getAvailableItems_returnsOnlyAvailable() {
        when(menuItemRepository.findByIsAvailableTrue()).thenReturn(List.of(menuItem));
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));

        List<MenuItemDTO> result = menuService.getAvailableItems();

        assertThat(result).hasSize(1);
    }

    @Test
    void createMenuItem_savesItemAndCreatesInventory() {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Tea");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("100"));

        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> {
            MenuItem saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryRepository.findByMenuItemId(2L)).thenReturn(Optional.of(inventory));

        MenuItemDTO result = menuService.createMenuItem(dto);

        assertThat(result.getName()).isEqualTo("Tea");
        verify(inventoryRepository).save(any(Inventory.class)); // inventory auto-created
    }

    @Test
    void createMenuItem_categoryNotFound_throwsNotFound() {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Tea");
        dto.setCategoryId(99L);
        dto.setPrice(new BigDecimal("100"));

        when(menuCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.createMenuItem(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMenuItem_partialUpdate_changesOnlyProvidedFields() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);
        when(inventoryRepository.findByMenuItemId(1L)).thenReturn(Optional.of(inventory));

        MenuItemDTO dto = new MenuItemDTO();
        dto.setPrice(new BigDecimal("200")); // only changing price

        MenuItemDTO result = menuService.updateMenuItem(1L, dto);

        assertThat(menuItem.getPrice()).isEqualByComparingTo("200");
        assertThat(menuItem.getName()).isEqualTo("Coffee"); // unchanged
    }

    @Test
    void getLowStockItems_returnsList() {
        when(inventoryRepository.findLowStockItems()).thenReturn(List.of(inventory));

        List<Inventory> result = menuService.getLowStockItems();

        assertThat(result).hasSize(1);
    }
}
