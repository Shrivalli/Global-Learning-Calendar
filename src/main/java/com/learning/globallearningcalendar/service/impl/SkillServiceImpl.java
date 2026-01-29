package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.SkillDTO;
import com.learning.globallearningcalendar.entity.Skill;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.SkillRepository;
import com.learning.globallearningcalendar.service.ISkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillServiceImpl implements ISkillService {

    private final SkillRepository skillRepository;

    @Override
    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SkillDTO> getActiveSkills() {
        return skillRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SkillDTO getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        return toDTO(skill);
    }

    @Override
    public SkillDTO getSkillByCode(String code) {
        Skill skill = skillRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "code", code));
        return toDTO(skill);
    }

    @Override
    public SkillDTO createSkill(SkillDTO dto) {
        Skill skill = toEntity(dto);
        skill.setIsActive(true);
        Skill saved = skillRepository.save(skill);
        return toDTO(saved);
    }

    @Override
    public SkillDTO updateSkill(Long id, SkillDTO dto) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        
        skill.setCode(dto.getCode());
        skill.setName(dto.getName());
        skill.setDescription(dto.getDescription());
        skill.setSkillCategory(dto.getSkillCategory());
        
        Skill updated = skillRepository.save(skill);
        return toDTO(updated);
    }

    @Override
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        skill.setIsActive(false);
        skillRepository.save(skill);
    }

    @Override
    public List<SkillDTO> getSkillsByCategory(String category) {
        return skillRepository.findActiveSkillsByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        return skillRepository.findAllActiveCategories();
    }

    @Override
    public List<SkillDTO> searchSkills(String searchTerm) {
        return skillRepository.searchByName(searchTerm).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private SkillDTO toDTO(Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .code(skill.getCode())
                .name(skill.getName())
                .description(skill.getDescription())
                .skillCategory(skill.getSkillCategory())
                .isActive(skill.getIsActive())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    private Skill toEntity(SkillDTO dto) {
        return Skill.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .skillCategory(dto.getSkillCategory())
                .build();
    }
}
