package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.UserDTO;
import com.learning.globallearningcalendar.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {

    List<UserDTO> getAllUsers();

    Page<UserDTO> getAllUsers(Pageable pageable);

    List<UserDTO> getActiveUsers();

    UserDTO getUserById(Long id);

    UserDTO getUserByEmployeeId(String employeeId);

    UserDTO getUserByEmail(String email);

    UserDTO createUser(UserDTO dto);

    UserDTO updateUser(Long id, UserDTO dto);

    void deleteUser(Long id);

    List<UserDTO> getUsersByBusinessUnit(Long buId);

    List<UserDTO> getUsersByLocation(Long locationId);

    List<UserDTO> getUsersByRole(Long roleId);

    List<UserDTO> getUsersByRoleType(Role.RoleType roleType);

    List<UserDTO> getDirectReports(Long managerId);

    List<UserDTO> searchUsers(String searchTerm);

    Long countUsersByBusinessUnit(Long buId);

    Long countUsersByLocation(Long locationId);
}
