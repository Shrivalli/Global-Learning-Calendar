package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.BusinessUnitDTO;
import com.learning.globallearningcalendar.service.IBusinessUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business-units")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Business Unit", description = "Business Unit management APIs")
public class BusinessUnitController {

    private final IBusinessUnitService businessUnitService;

    @GetMapping
    @Operation(summary = "Get all business units")
    public ResponseEntity<List<BusinessUnitDTO>> getAllBusinessUnits() {
        return ResponseEntity.ok(businessUnitService.getAllBusinessUnits());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active business units")
    public ResponseEntity<List<BusinessUnitDTO>> getActiveBusinessUnits() {
        return ResponseEntity.ok(businessUnitService.getActiveBusinessUnits());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get business unit by ID")
    public ResponseEntity<BusinessUnitDTO> getBusinessUnitById(@PathVariable Long id) {
        return ResponseEntity.ok(businessUnitService.getBusinessUnitById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get business unit by code")
    public ResponseEntity<BusinessUnitDTO> getBusinessUnitByCode(@PathVariable String code) {
        return ResponseEntity.ok(businessUnitService.getBusinessUnitByCode(code));
    }

    @PostMapping
    @Operation(summary = "Create a new business unit")
    public ResponseEntity<BusinessUnitDTO> createBusinessUnit(@Valid @RequestBody BusinessUnitDTO dto) {
        return new ResponseEntity<>(businessUnitService.createBusinessUnit(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a business unit")
    public ResponseEntity<BusinessUnitDTO> updateBusinessUnit(@PathVariable Long id, @Valid @RequestBody BusinessUnitDTO dto) {
        return ResponseEntity.ok(businessUnitService.updateBusinessUnit(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a business unit (soft delete)")
    public ResponseEntity<Void> deleteBusinessUnit(@PathVariable Long id) {
        businessUnitService.deleteBusinessUnit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/top-level")
    @Operation(summary = "Get top-level business units (no parent)")
    public ResponseEntity<List<BusinessUnitDTO>> getTopLevelBusinessUnits() {
        return ResponseEntity.ok(businessUnitService.getTopLevelBusinessUnits());
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "Get child business units")
    public ResponseEntity<List<BusinessUnitDTO>> getChildBusinessUnits(@PathVariable Long parentId) {
        return ResponseEntity.ok(businessUnitService.getChildBusinessUnits(parentId));
    }

    @GetMapping("/{id}/user-count")
    @Operation(summary = "Get user count for a business unit")
    public ResponseEntity<Long> countUsersByBusinessUnit(@PathVariable Long id) {
        return ResponseEntity.ok(businessUnitService.countUsersByBusinessUnit(id));
    }
}
