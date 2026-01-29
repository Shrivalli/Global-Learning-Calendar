package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.SkillDTO;

import java.util.List;

public interface ISkillService {

    List<SkillDTO> getAllSkills();

    List<SkillDTO> getActiveSkills();

    SkillDTO getSkillById(Long id);

    SkillDTO getSkillByCode(String code);

    SkillDTO createSkill(SkillDTO dto);

    SkillDTO updateSkill(Long id, SkillDTO dto);

    void deleteSkill(Long id);

    List<SkillDTO> getSkillsByCategory(String category);

    List<String> getAllCategories();

    List<SkillDTO> searchSkills(String searchTerm);
}
