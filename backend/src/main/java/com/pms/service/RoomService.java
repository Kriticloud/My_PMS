package com.pms.service;

import com.pms.dto.RoomDTO;
import com.pms.entity.Room;
import com.pms.entity.RoomType;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.RoomRepository;
import com.pms.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        return toDTO(findRoom(id));
    }

    public List<RoomDTO> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDTO createRoom(RoomDTO dto) {
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new BadRequestException("Room number already exists: " + dto.getRoomNumber());
        }
        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found"));

        Room room = Room.builder()
                .roomNumber(dto.getRoomNumber())
                .roomType(roomType)
                .floor(dto.getFloor() != null ? dto.getFloor() : 1)
                .status("AVAILABLE")
                .build();
        return toDTO(roomRepository.save(room));
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room room = findRoom(id);
        if (dto.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room type not found"));
            room.setRoomType(roomType);
        }
        if (dto.getFloor() != null)
            room.setFloor(dto.getFloor());
        if (dto.getStatus() != null)
            room.setStatus(dto.getStatus());
        return toDTO(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = findRoom(id);
        if ("OCCUPIED".equals(room.getStatus())) {
            throw new BadRequestException("Cannot delete an occupied room");
        }
        roomRepository.delete(room);
    }

    @Transactional
    public RoomDTO updateRoomStatus(Long id, String status) {
        Room room = findRoom(id);
        room.setStatus(status);
        return toDTO(roomRepository.save(room));
    }

    @Cacheable("roomTypes")
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    private RoomDTO toDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomTypeId(room.getRoomType().getId());
        dto.setRoomTypeName(room.getRoomType().getName());
        dto.setBasePrice(room.getRoomType().getBasePrice());
        dto.setFloor(room.getFloor());
        dto.setStatus(room.getStatus());
        return dto;
    }
}
