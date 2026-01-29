package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.LocationDTO;
import com.learning.globallearningcalendar.service.ILocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Location", description = "Location management APIs")
public class LocationController {

    private final ILocationService locationService;

    @GetMapping
    @Operation(summary = "Get all locations")
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active locations")
    public ResponseEntity<List<LocationDTO>> getActiveLocations() {
        return ResponseEntity.ok(locationService.getActiveLocations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new location")
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO dto) {
        return new ResponseEntity<>(locationService.createLocation(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a location")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationDTO dto) {
        return ResponseEntity.ok(locationService.updateLocation(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a location (soft delete)")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Get locations by country")
    public ResponseEntity<List<LocationDTO>> getLocationsByCountry(@PathVariable String country) {
        return ResponseEntity.ok(locationService.getLocationsByCountry(country));
    }

    @GetMapping("/countries")
    @Operation(summary = "Get all countries with active locations")
    public ResponseEntity<List<String>> getAllCountries() {
        return ResponseEntity.ok(locationService.getAllCountries());
    }

    @GetMapping("/regions")
    @Operation(summary = "Get all regions with active locations")
    public ResponseEntity<List<String>> getAllRegions() {
        return ResponseEntity.ok(locationService.getAllRegions());
    }
}
