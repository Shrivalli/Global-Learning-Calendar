package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.LearningProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgramRepository extends JpaRepository<LearningProgram, Long> {

    Optional<LearningProgram> findByCode(String code);

    Optional<LearningProgram> findByName(String name);

    List<LearningProgram> findByIsActiveTrue();

    Page<LearningProgram> findByIsActiveTrue(Pageable pageable);

    List<LearningProgram> findByProgramType(LearningProgram.ProgramType programType);

    List<LearningProgram> findByDeliveryMode(LearningProgram.DeliveryMode deliveryMode);

    List<LearningProgram> findByIsMandatoryTrue();

    @Query("SELECT lp FROM LearningProgram lp WHERE lp.programType = :programType AND lp.isActive = true")
    List<LearningProgram> findActiveProgramsByType(@Param("programType") LearningProgram.ProgramType programType);

    @Query("SELECT lp FROM LearningProgram lp WHERE lp.deliveryMode = :deliveryMode AND lp.isActive = true")
    List<LearningProgram> findActiveProgramsByDeliveryMode(@Param("deliveryMode") LearningProgram.DeliveryMode deliveryMode);

    @Query("SELECT lp FROM LearningProgram lp JOIN lp.skills s WHERE s.id = :skillId AND lp.isActive = true")
    List<LearningProgram> findBySkillId(@Param("skillId") Long skillId);

    @Query("SELECT lp FROM LearningProgram lp JOIN lp.targetRoles r WHERE r.id = :roleId AND lp.isActive = true")
    List<LearningProgram> findByTargetRoleId(@Param("roleId") Long roleId);

    @Query("SELECT lp FROM LearningProgram lp JOIN lp.targetBusinessUnits bu WHERE bu.id = :buId AND lp.isActive = true")
    List<LearningProgram> findByTargetBusinessUnitId(@Param("buId") Long buId);

    @Query("SELECT lp FROM LearningProgram lp WHERE " +
           "(LOWER(lp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lp.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lp.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND lp.isActive = true")
    List<LearningProgram> searchPrograms(@Param("searchTerm") String searchTerm);

    @Query("SELECT lp FROM LearningProgram lp LEFT JOIN FETCH lp.skills LEFT JOIN FETCH lp.targetRoles WHERE lp.id = :id")
    Optional<LearningProgram> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(ls) FROM LearningSession ls WHERE ls.learningProgram.id = :programId AND ls.isActive = true")
    Long countSessionsByProgram(@Param("programId") Long programId);
}
