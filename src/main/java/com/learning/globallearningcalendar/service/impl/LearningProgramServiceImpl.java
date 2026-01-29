package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.LearningProgramDTO;
import com.learning.globallearningcalendar.dto.RoleDTO;
import com.learning.globallearningcalendar.dto.SkillDTO;
import com.learning.globallearningcalendar.entity.*;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.*;
import com.learning.globallearningcalendar.service.ILearningProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningProgramServiceImpl implements ILearningProgramService {

    private final LearningProgramRepository learningProgramRepository;
    private final SkillRepository skillRepository;
    private final RoleRepository roleRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;

    @Override
    public List<LearningProgramDTO> getAllPrograms() {
        return learningProgramRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<LearningProgramDTO> getAllPrograms(Pageable pageable) {
        return learningProgramRepository.findByIsActiveTrue(pageable).map(this::toDTO);
    }

    @Override
    public List<LearningProgramDTO> getActivePrograms() {
        return learningProgramRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LearningProgramDTO getProgramById(Long id) {
        LearningProgram program = learningProgramRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("LearningProgram", "id", id));
        return toDTO(program);
    }

    @Override
    public LearningProgramDTO getProgramByCode(String code) {
        LearningProgram program = learningProgramRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("LearningProgram", "code", code));
        return toDTO(program);
    }

    @Override
    public LearningProgramDTO createProgram(LearningProgramDTO dto) {
        LearningProgram program = new LearningProgram();
        program.setCode(dto.getCode());
        program.setName(dto.getName());
        program.setDescription(dto.getDescription());
        program.setProgramType(dto.getProgramType());
        program.setDeliveryMode(dto.getDeliveryMode());
        program.setDurationHours(dto.getDurationHours());
        program.setIsMandatory(dto.getIsMandatory() != null ? dto.getIsMandatory() : false);
        program.setIsActive(true);

        if (dto.getSkillIds() != null && !dto.getSkillIds().isEmpty()) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(dto.getSkillIds()));
            program.setSkills(skills);
        }

        if (dto.getTargetRoleIds() != null && !dto.getTargetRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getTargetRoleIds()));
            program.setTargetRoles(roles);
        }

        if (dto.getTargetBusinessUnitIds() != null && !dto.getTargetBusinessUnitIds().isEmpty()) {
            Set<BusinessUnit> businessUnits = new HashSet<>(businessUnitRepository.findAllById(dto.getTargetBusinessUnitIds()));
            program.setTargetBusinessUnits(businessUnits);
        }

        if (dto.getCreatedById() != null) {
            User createdBy = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCreatedById()));
            program.setCreatedBy(createdBy);
        }

        LearningProgram saved = learningProgramRepository.save(program);
        return toDTO(saved);
    }

    @Override
    public LearningProgramDTO updateProgram(Long id, LearningProgramDTO dto) {
        LearningProgram program = learningProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LearningProgram", "id", id));

        program.setCode(dto.getCode());
        program.setName(dto.getName());
        program.setDescription(dto.getDescription());
        program.setProgramType(dto.getProgramType());
        program.setDeliveryMode(dto.getDeliveryMode());
        program.setDurationHours(dto.getDurationHours());
        program.setIsMandatory(dto.getIsMandatory());

        if (dto.getSkillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(dto.getSkillIds()));
            program.setSkills(skills);
        }

        if (dto.getTargetRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getTargetRoleIds()));
            program.setTargetRoles(roles);
        }

        if (dto.getTargetBusinessUnitIds() != null) {
            Set<BusinessUnit> businessUnits = new HashSet<>(businessUnitRepository.findAllById(dto.getTargetBusinessUnitIds()));
            program.setTargetBusinessUnits(businessUnits);
        }

        LearningProgram updated = learningProgramRepository.save(program);
        return toDTO(updated);
    }

    @Override
    public void deleteProgram(Long id) {
        LearningProgram program = learningProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LearningProgram", "id", id));
        program.setIsActive(false);
        learningProgramRepository.save(program);
    }

    @Override
    public List<LearningProgramDTO> getProgramsByType(LearningProgram.ProgramType programType) {
        return learningProgramRepository.findActiveProgramsByType(programType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningProgramDTO> getProgramsByDeliveryMode(LearningProgram.DeliveryMode deliveryMode) {
        return learningProgramRepository.findActiveProgramsByDeliveryMode(deliveryMode).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningProgramDTO> getMandatoryPrograms() {
        return learningProgramRepository.findByIsMandatoryTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningProgramDTO> getProgramsBySkill(Long skillId) {
        return learningProgramRepository.findBySkillId(skillId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningProgramDTO> getProgramsByTargetRole(Long roleId) {
        return learningProgramRepository.findByTargetRoleId(roleId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningProgramDTO> getProgramsByTargetBusinessUnit(Long buId) {
        return learningProgramRepository.findByTargetBusinessUnitId(buId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningProgramDTO> searchPrograms(String searchTerm) {
        return learningProgramRepository.searchPrograms(searchTerm).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countSessionsByProgram(Long programId) {
        return learningProgramRepository.countSessionsByProgram(programId);
    }

    private LearningProgramDTO toDTO(LearningProgram program) {
        LearningProgramDTO dto = LearningProgramDTO.builder()
                .id(program.getId())
                .code(program.getCode())
                .name(program.getName())
                .description(program.getDescription())
                .programType(program.getProgramType())
                .deliveryMode(program.getDeliveryMode())
                .durationHours(program.getDurationHours())
                .isMandatory(program.getIsMandatory())
                .isActive(program.getIsActive())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();

        if (program.getSkills() != null && !program.getSkills().isEmpty()) {
            dto.setSkillIds(program.getSkills().stream().map(Skill::getId).collect(Collectors.toSet()));
            dto.setSkills(program.getSkills().stream()
                    .map(s -> SkillDTO.builder()
                            .id(s.getId())
                            .code(s.getCode())
                            .name(s.getName())
                            .skillCategory(s.getSkillCategory())
                            .build())
                    .collect(Collectors.toSet()));
        }

        if (program.getTargetRoles() != null && !program.getTargetRoles().isEmpty()) {
            dto.setTargetRoleIds(program.getTargetRoles().stream().map(Role::getId).collect(Collectors.toSet()));
            dto.setTargetRoles(program.getTargetRoles().stream()
                    .map(r -> RoleDTO.builder()
                            .id(r.getId())
                            .code(r.getCode())
                            .name(r.getName())
                            .roleType(r.getRoleType())
                            .build())
                    .collect(Collectors.toSet()));
        }

        if (program.getCreatedBy() != null) {
            dto.setCreatedById(program.getCreatedBy().getId());
            dto.setCreatedByName(program.getCreatedBy().getFullName());
        }

        return dto;
    }
}
