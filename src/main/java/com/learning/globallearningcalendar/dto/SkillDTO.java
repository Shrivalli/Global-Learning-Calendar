package com.learning.globallearningcalendar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDTO {

    private Long id;

    @NotBlank(message = "Skill code is required")
    private String code;

    @NotBlank(message = "Skill name is required")
    private String name;

    private String description;

    private String skillCategory;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
