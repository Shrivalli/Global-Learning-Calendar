package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.LearningProgramDTO;
import com.learning.globallearningcalendar.entity.LearningProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ILearningProgramService {

    List<LearningProgramDTO> getAllPrograms();

    Page<LearningProgramDTO> getAllPrograms(Pageable pageable);

    List<LearningProgramDTO> getActivePrograms();

    LearningProgramDTO getProgramById(Long id);

    LearningProgramDTO getProgramByCode(String code);

    LearningProgramDTO createProgram(LearningProgramDTO dto);

    LearningProgramDTO updateProgram(Long id, LearningProgramDTO dto);

    void deleteProgram(Long id);

    List<LearningProgramDTO> getProgramsByType(LearningProgram.ProgramType programType);

    List<LearningProgramDTO> getProgramsByDeliveryMode(LearningProgram.DeliveryMode deliveryMode);

    List<LearningProgramDTO> getMandatoryPrograms();

    List<LearningProgramDTO> getProgramsBySkill(Long skillId);

    List<LearningProgramDTO> getProgramsByTargetRole(Long roleId);

    List<LearningProgramDTO> getProgramsByTargetBusinessUnit(Long buId);

    List<LearningProgramDTO> searchPrograms(String searchTerm);

    Long countSessionsByProgram(Long programId);
}
