package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    Optional<Role> findByName(String name);

    List<Role> findByIsActiveTrue();

    List<Role> findByRoleType(Role.RoleType roleType);

    @Query("SELECT r FROM Role r WHERE r.roleType = :roleType AND r.isActive = true")
    List<Role> findActiveRolesByType(@Param("roleType") Role.RoleType roleType);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId AND u.isActive = true")
    Long countActiveUsersByRole(@Param("roleId") Long roleId);
}
