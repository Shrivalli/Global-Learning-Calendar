package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.RoleDTO;
import com.learning.globallearningcalendar.entity.Role;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.RoleRepository;
import com.learning.globallearningcalendar.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements IRoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleDTO> getActiveRoles() {
        return roleRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return toDTO(role);
    }

    @Override
    public RoleDTO getRoleByCode(String code) {
        Role role = roleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", code));
        return toDTO(role);
    }

    @Override
    public RoleDTO createRole(RoleDTO dto) {
        Role role = toEntity(dto);
        role.setIsActive(true);
        Role saved = roleRepository.save(role);
        return toDTO(saved);
    }

    @Override
    public RoleDTO updateRole(Long id, RoleDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setRoleType(dto.getRoleType());
        
        Role updated = roleRepository.save(role);
        return toDTO(updated);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        role.setIsActive(false);
        roleRepository.save(role);
    }

    @Override
    public List<RoleDTO> getRolesByType(Role.RoleType roleType) {
        return roleRepository.findActiveRolesByType(roleType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countUsersByRole(Long roleId) {
        return roleRepository.countActiveUsersByRole(roleId);
    }

    private RoleDTO toDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .roleType(role.getRoleType())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private Role toEntity(RoleDTO dto) {
        return Role.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .roleType(dto.getRoleType())
                .build();
    }
}
