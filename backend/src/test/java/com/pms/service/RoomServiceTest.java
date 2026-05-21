package com.pms.service;

import com.pms.dto.RoomDTO;
import com.pms.entity.Room;
import com.pms.entity.RoomType;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.RoomRepository;
import com.pms.repository.RoomTypeRepository;
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
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private RoomService roomService;

    private RoomType roomType;
    private Room room;

    @BeforeEach
    void setUp() {
        roomType = RoomType.builder()
                .id(1L)
                .name("Deluxe")
                .basePrice(new BigDecimal("3000"))
                .maxOccupancy(2)
                .build();

        room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(roomType)
                .floor(1)
                .status("AVAILABLE")
                .build();
    }

    @Test
    void getAllRooms_returnsMappedDTOs() {
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<RoomDTO> result = roomService.getAllRooms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomNumber()).isEqualTo("101");
        assertThat(result.get(0).getRoomTypeName()).isEqualTo("Deluxe");
        assertThat(result.get(0).getBasePrice()).isEqualByComparingTo("3000");
    }

    @Test
    void getRoomById_existingRoom_returnsDTO() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomDTO result = roomService.getRoomById(1L);

        assertThat(result.getRoomNumber()).isEqualTo("101");
        assertThat(result.getFloor()).isEqualTo(1);
    }

    @Test
    void getRoomById_notFound_throwsException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getRoomsByStatus_filtersCorrectly() {
        when(roomRepository.findByStatus("AVAILABLE")).thenReturn(List.of(room));

        List<RoomDTO> result = roomService.getRoomsByStatus("AVAILABLE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void createRoom_duplicateRoomNumber_throwsException() {
        RoomDTO dto = new RoomDTO();
        dto.setRoomNumber("101");
        dto.setRoomTypeId(1L);
        dto.setFloor(1);

        when(roomRepository.existsByRoomNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createRoom_valid_savesAndReturnsDTO() {
        RoomDTO dto = new RoomDTO();
        dto.setRoomNumber("102");
        dto.setRoomTypeId(1L);
        dto.setFloor(2);

        when(roomRepository.existsByRoomNumber("102")).thenReturn(false);
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
            Room saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        RoomDTO result = roomService.createRoom(dto);

        assertThat(result.getRoomNumber()).isEqualTo("102");
        assertThat(result.getFloor()).isEqualTo(2);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void updateRoomStatus_changesStatus() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        RoomDTO result = roomService.updateRoomStatus(1L, "MAINTENANCE");

        assertThat(room.getStatus()).isEqualTo("MAINTENANCE");
        verify(roomRepository).save(room);
    }

    @Test
    void deleteRoom_existingRoom_deletes() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.deleteRoom(1L);

        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_notFound_throwsException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.deleteRoom(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllRoomTypes_returnsList() {
        when(roomTypeRepository.findAll()).thenReturn(List.of(roomType));

        List<RoomType> result = roomService.getAllRoomTypes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Deluxe");
    }
}
