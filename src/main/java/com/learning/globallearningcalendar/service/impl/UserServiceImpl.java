package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.UserDTO;
import com.learning.globallearningcalendar.entity.BusinessUnit;
import com.learning.globallearningcalendar.entity.Location;
import com.learning.globallearningcalendar.entity.Role;
import com.learning.globallearningcalendar.entity.User;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.BusinessUnitRepository;
import com.learning.globallearningcalendar.repository.LocationRepository;
import com.learning.globallearningcalendar.repository.RoleRepository;
import com.learning.globallearningcalendar.repository.UserRepository;
import com.learning.globallearningcalendar.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final LocationRepository locationRepository;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrue(pageable).map(this::toDTO);
    }

    @Override
    public List<UserDTO> getActiveUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toDTO(user);
    }

    @Override
    public UserDTO getUserByEmployeeId(String employeeId) {
        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "employeeId", employeeId));
        return toDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return toDTO(user);
    }

    @Override
    public UserDTO createUser(UserDTO dto) {
        User user = new User();
        user.setEmployeeId(dto.getEmployeeId());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setJobTitle(dto.getJobTitle());
        user.setIsActive(true);

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", dto.getRoleId()));
            user.setRole(role);
        }

        if (dto.getBusinessUnitId() != null) {
            BusinessUnit bu = businessUnitRepository.findById(dto.getBusinessUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", dto.getBusinessUnitId()));
            user.setBusinessUnit(bu);
        }

        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", "id", dto.getLocationId()));
            user.setLocation(location);
        }

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", dto.getManagerId()));
            user.setManager(manager);
        }

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setEmployeeId(dto.getEmployeeId());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setJobTitle(dto.getJobTitle());

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", dto.getRoleId()));
            user.setRole(role);
        }

        if (dto.getBusinessUnitId() != null) {
            BusinessUnit bu = businessUnitRepository.findById(dto.getBusinessUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", dto.getBusinessUnitId()));
            user.setBusinessUnit(bu);
        }

        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", "id", dto.getLocationId()));
            user.setLocation(location);
        }

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", dto.getManagerId()));
            user.setManager(manager);
        } else {
            user.setManager(null);
        }

        User updated = userRepository.save(user);
        return toDTO(updated);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public List<UserDTO> getUsersByBusinessUnit(Long buId) {
        return userRepository.findByBusinessUnitId(buId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByLocation(Long locationId) {
        return userRepository.findByLocationId(locationId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByRole(Long roleId) {
        return userRepository.findByRoleId(roleId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByRoleType(Role.RoleType roleType) {
        return userRepository.findByRoleType(roleType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getDirectReports(Long managerId) {
        return userRepository.findDirectReports(managerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countUsersByBusinessUnit(Long buId) {
        return userRepository.countByBusinessUnit(buId);
    }

    @Override
    public Long countUsersByLocation(Long locationId) {
        return userRepository.countByLocation(locationId);
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .jobTitle(user.getJobTitle())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getName());
            dto.setRoleType(user.getRole().getRoleType().name());
        }

        if (user.getBusinessUnit() != null) {
            dto.setBusinessUnitId(user.getBusinessUnit().getId());
            dto.setBusinessUnitName(user.getBusinessUnit().getName());
        }

        if (user.getLocation() != null) {
            dto.setLocationId(user.getLocation().getId());
            dto.setLocationName(user.getLocation().getName());
        }

        if (user.getManager() != null) {
            dto.setManagerId(user.getManager().getId());
            dto.setManagerName(user.getManager().getFullName());
        }

        return dto;
    }
}
