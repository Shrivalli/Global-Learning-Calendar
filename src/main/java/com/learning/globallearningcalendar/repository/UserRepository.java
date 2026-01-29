package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmployeeId(String employeeId);

    Optional<User> findByEmail(String email);

    List<User> findByIsActiveTrue();

    Page<User> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.businessUnit.id = :buId AND u.isActive = true")
    List<User> findByBusinessUnitId(@Param("buId") Long buId);

    @Query("SELECT u FROM User u WHERE u.location.id = :locationId AND u.isActive = true")
    List<User> findByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT u FROM User u WHERE u.role.id = :roleId AND u.isActive = true")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId AND u.isActive = true")
    List<User> findDirectReports(@Param("managerId") Long managerId);

    @Query("SELECT u FROM User u WHERE u.role.roleType = :roleType AND u.isActive = true")
    List<User> findByRoleType(@Param("roleType") com.learning.globallearningcalendar.entity.Role.RoleType roleType);

    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND u.isActive = true")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role LEFT JOIN FETCH u.businessUnit LEFT JOIN FETCH u.location WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(u) FROM User u WHERE u.businessUnit.id = :buId AND u.isActive = true")
    Long countByBusinessUnit(@Param("buId") Long buId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.location.id = :locationId AND u.isActive = true")
    Long countByLocation(@Param("locationId") Long locationId);
}
