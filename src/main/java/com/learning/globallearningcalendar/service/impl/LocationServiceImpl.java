package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.LocationDTO;
import com.learning.globallearningcalendar.entity.Location;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.LocationRepository;
import com.learning.globallearningcalendar.service.ILocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationServiceImpl implements ILocationService {

    private final LocationRepository locationRepository;

    @Override
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LocationDTO> getActiveLocations() {
        return locationRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LocationDTO getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        return toDTO(location);
    }

    @Override
    public LocationDTO createLocation(LocationDTO dto) {
        Location location = toEntity(dto);
        location.setIsActive(true);
        Location saved = locationRepository.save(location);
        return toDTO(saved);
    }

    @Override
    public LocationDTO updateLocation(Long id, LocationDTO dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        
        location.setName(dto.getName());
        location.setCity(dto.getCity());
        location.setCountry(dto.getCountry());
        location.setRegion(dto.getRegion());
        location.setTimezone(dto.getTimezone());
        location.setAddress(dto.getAddress());
        location.setCapacity(dto.getCapacity());
        
        Location updated = locationRepository.save(location);
        return toDTO(updated);
    }

    @Override
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
        location.setIsActive(false);
        locationRepository.save(location);
    }

    @Override
    public List<LocationDTO> getLocationsByCountry(String country) {
        return locationRepository.findActiveLocationsByCountry(country).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCountries() {
        return locationRepository.findAllActiveCountries();
    }

    @Override
    public List<String> getAllRegions() {
        return locationRepository.findAllActiveRegions();
    }

    private LocationDTO toDTO(Location location) {
        return LocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .city(location.getCity())
                .country(location.getCountry())
                .region(location.getRegion())
                .timezone(location.getTimezone())
                .address(location.getAddress())
                .capacity(location.getCapacity())
                .isActive(location.getIsActive())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }

    private Location toEntity(LocationDTO dto) {
        return Location.builder()
                .name(dto.getName())
                .city(dto.getCity())
                .country(dto.getCountry())
                .region(dto.getRegion())
                .timezone(dto.getTimezone())
                .address(dto.getAddress())
                .capacity(dto.getCapacity())
                .build();
    }
}
