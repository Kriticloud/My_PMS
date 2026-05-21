package com.pms.service;

import com.pms.dto.GuestDTO;
import com.pms.entity.Guest;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.GuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private GuestService guestService;

    private Guest guest;

    @BeforeEach
    void setUp() {
        guest = Guest.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .idType("PASSPORT")
                .idNumber("AB123456")
                .build();
    }

    @Test
    void getAllGuests_returnsMappedDTOs() {
        when(guestRepository.findAll()).thenReturn(List.of(guest));

        List<GuestDTO> result = guestService.getAllGuests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    void getGuestById_existingGuest_returnsDTO() {
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));

        GuestDTO result = guestService.getGuestById(1L);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getGuestById_notFound_throwsException() {
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.getGuestById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchGuests_findsMatchingGuests() {
        when(guestRepository.searchByName("doe")).thenReturn(List.of(guest));

        List<GuestDTO> result = guestService.searchGuests("doe");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    void createGuest_savesAndReturnsDTO() {
        GuestDTO dto = new GuestDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane@example.com");

        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> {
            Guest saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        GuestDTO result = guestService.createGuest(dto);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        verify(guestRepository).save(any(Guest.class));
    }

    @Test
    void updateGuest_partialUpdate_onlyChangesProvidedFields() {
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);

        GuestDTO dto = new GuestDTO();
        dto.setPhone("1111111111");
        // firstName and lastName are null — should NOT be overwritten

        GuestDTO result = guestService.updateGuest(1L, dto);

        assertThat(guest.getPhone()).isEqualTo("1111111111");
        assertThat(guest.getFirstName()).isEqualTo("John"); // unchanged
    }

    @Test
    void deleteGuest_existingGuest_deletes() {
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));

        guestService.deleteGuest(1L);

        verify(guestRepository).delete(guest);
    }
}
