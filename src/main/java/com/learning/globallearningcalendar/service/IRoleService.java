package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.RoleDTO;
import com.learning.globallearningcalendar.entity.Role;

import java.util.List;

public interface IRoleService {

    List<RoleDTO> getAllRoles();

    List<RoleDTO> getActiveRoles();

    RoleDTO getRoleById(Long id);

    RoleDTO getRoleByCode(String code);

    RoleDTO createRole(RoleDTO dto);

    RoleDTO updateRole(Long id, RoleDTO dto);

    void deleteRole(Long id);

    List<RoleDTO> getRolesByType(Role.RoleType roleType);

    Long countUsersByRole(Long roleId);
}
