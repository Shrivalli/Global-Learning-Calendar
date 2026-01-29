package com.learning.globallearningcalendar.dto;

import com.learning.globallearningcalendar.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {

    private Long id;

    @NotBlank(message = "Role code is required")
    private String code;

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    @NotNull(message = "Role type is required")
    private Role.RoleType roleType;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
