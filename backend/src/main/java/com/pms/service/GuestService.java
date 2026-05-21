package com.pms.service;

import com.pms.dto.GuestDTO;
import com.pms.entity.Guest;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    public List<GuestDTO> getAllGuests() {
        return guestRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public GuestDTO getGuestById(Long id) {
        return toDTO(findGuest(id));
    }

    public List<GuestDTO> searchGuests(String name) {
        return guestRepository.searchByName(name).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public GuestDTO createGuest(GuestDTO dto) {
        Guest guest = Guest.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .idType(dto.getIdType())
                .idNumber(dto.getIdNumber())
                .address(dto.getAddress())
                .build();
        return toDTO(guestRepository.save(guest));
    }

    @Transactional
    public GuestDTO updateGuest(Long id, GuestDTO dto) {
        Guest guest = findGuest(id);
        if (dto.getFirstName() != null)
            guest.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            guest.setLastName(dto.getLastName());
        if (dto.getEmail() != null)
            guest.setEmail(dto.getEmail());
        if (dto.getPhone() != null)
            guest.setPhone(dto.getPhone());
        if (dto.getIdType() != null)
            guest.setIdType(dto.getIdType());
        if (dto.getIdNumber() != null)
            guest.setIdNumber(dto.getIdNumber());
        if (dto.getAddress() != null)
            guest.setAddress(dto.getAddress());
        return toDTO(guestRepository.save(guest));
    }

    @Transactional
    public void deleteGuest(Long id) {
        Guest guest = findGuest(id);
        guestRepository.delete(guest);
    }

    private Guest findGuest(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
    }

    private GuestDTO toDTO(Guest g) {
        GuestDTO dto = new GuestDTO();
        dto.setId(g.getId());
        dto.setFirstName(g.getFirstName());
        dto.setLastName(g.getLastName());
        dto.setEmail(g.getEmail());
        dto.setPhone(g.getPhone());
        dto.setIdType(g.getIdType());
        dto.setIdNumber(g.getIdNumber());
        dto.setAddress(g.getAddress());
        return dto;
    }
}
