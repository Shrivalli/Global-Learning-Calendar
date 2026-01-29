package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.RoleDTO;
import com.learning.globallearningcalendar.entity.Role;
import com.learning.globallearningcalendar.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Role", description = "Role management APIs")
public class RoleController {

    private final IRoleService roleService;

    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active roles")
    public ResponseEntity<List<RoleDTO>> getActiveRoles() {
        return ResponseEntity.ok(roleService.getActiveRoles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get role by code")
    public ResponseEntity<RoleDTO> getRoleByCode(@PathVariable String code) {
        return ResponseEntity.ok(roleService.getRoleByCode(code));
    }

    @PostMapping
    @Operation(summary = "Create a new role")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO dto) {
        return new ResponseEntity<>(roleService.createRole(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a role")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        return ResponseEntity.ok(roleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role (soft delete)")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{roleType}")
    @Operation(summary = "Get roles by type")
    public ResponseEntity<List<RoleDTO>> getRolesByType(@PathVariable Role.RoleType roleType) {
        return ResponseEntity.ok(roleService.getRolesByType(roleType));
    }

    @GetMapping("/{id}/user-count")
    @Operation(summary = "Get user count for a role")
    public ResponseEntity<Long> countUsersByRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.countUsersByRole(id));
    }
}
