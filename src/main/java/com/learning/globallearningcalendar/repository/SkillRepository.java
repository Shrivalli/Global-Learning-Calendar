package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByCode(String code);

    Optional<Skill> findByName(String name);

    List<Skill> findByIsActiveTrue();

    List<Skill> findBySkillCategory(String skillCategory);

    @Query("SELECT DISTINCT s.skillCategory FROM Skill s WHERE s.isActive = true AND s.skillCategory IS NOT NULL ORDER BY s.skillCategory")
    List<String> findAllActiveCategories();

    @Query("SELECT s FROM Skill s WHERE s.skillCategory = :category AND s.isActive = true")
    List<Skill> findActiveSkillsByCategory(@Param("category") String category);

    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND s.isActive = true")
    List<Skill> searchByName(@Param("searchTerm") String searchTerm);

    @Query("SELECT s FROM Skill s JOIN s.learningPrograms lp WHERE lp.id = :programId")
    List<Skill> findByLearningProgramId(@Param("programId") Long programId);
}
