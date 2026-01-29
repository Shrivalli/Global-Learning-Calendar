package com.learning.globallearningcalendar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessUnitDTO {

    private Long id;

    @NotBlank(message = "Business unit code is required")
    private String code;

    @NotBlank(message = "Business unit name is required")
    private String name;

    private String description;

    private Long parentBusinessUnitId;

    private String parentBusinessUnitName;

    private List<BusinessUnitDTO> childBusinessUnits;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
