package com.learning.globallearningcalendar.dto;

import com.learning.globallearningcalendar.entity.LearningProgram;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgramDTO {

    private Long id;

    @NotBlank(message = "Program code is required")
    private String code;

    @NotBlank(message = "Program name is required")
    private String name;

    private String description;

    private LearningProgram.ProgramType programType;

    private LearningProgram.DeliveryMode deliveryMode;

    private Integer durationHours;

    private Set<Long> skillIds;

    private Set<SkillDTO> skills;

    private Set<Long> targetRoleIds;

    private Set<RoleDTO> targetRoles;

    private Set<Long> targetBusinessUnitIds;

    private Set<BusinessUnitDTO> targetBusinessUnits;

    private Long createdById;

    private String createdByName;

    private Boolean isMandatory;

    private Boolean isActive;

    private Long sessionCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
