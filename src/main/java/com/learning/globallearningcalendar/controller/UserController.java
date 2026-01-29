package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.UserDTO;
import com.learning.globallearningcalendar.entity.Role;
import com.learning.globallearningcalendar.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "User", description = "User management APIs")
public class UserController {

    private final IUserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<Page<UserDTO>> getAllUsersPaged(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active users")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/employee-id/{employeeId}")
    @Operation(summary = "Get user by employee ID")
    public ResponseEntity<UserDTO> getUserByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(userService.getUserByEmployeeId(employeeId));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        return new ResponseEntity<>(userService.createUser(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user (soft delete)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/business-unit/{buId}")
    @Operation(summary = "Get users by business unit")
    public ResponseEntity<List<UserDTO>> getUsersByBusinessUnit(@PathVariable Long buId) {
        return ResponseEntity.ok(userService.getUsersByBusinessUnit(buId));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get users by location")
    public ResponseEntity<List<UserDTO>> getUsersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(userService.getUsersByLocation(locationId));
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Get users by role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(userService.getUsersByRole(roleId));
    }

    @GetMapping("/role-type/{roleType}")
    @Operation(summary = "Get users by role type")
    public ResponseEntity<List<UserDTO>> getUsersByRoleType(@PathVariable Role.RoleType roleType) {
        return ResponseEntity.ok(userService.getUsersByRoleType(roleType));
    }

    @GetMapping("/{managerId}/direct-reports")
    @Operation(summary = "Get direct reports of a manager")
    public ResponseEntity<List<UserDTO>> getDirectReports(@PathVariable Long managerId) {
        return ResponseEntity.ok(userService.getDirectReports(managerId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name, email, or employee ID")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String searchTerm) {
        return ResponseEntity.ok(userService.searchUsers(searchTerm));
    }

    @GetMapping("/business-unit/{buId}/count")
    @Operation(summary = "Get user count by business unit")
    public ResponseEntity<Long> countUsersByBusinessUnit(@PathVariable Long buId) {
        return ResponseEntity.ok(userService.countUsersByBusinessUnit(buId));
    }

    @GetMapping("/location/{locationId}/count")
    @Operation(summary = "Get user count by location")
    public ResponseEntity<Long> countUsersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(userService.countUsersByLocation(locationId));
    }
}
