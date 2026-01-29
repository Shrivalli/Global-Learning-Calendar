package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.BusinessUnitDTO;
import com.learning.globallearningcalendar.entity.BusinessUnit;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.BusinessUnitRepository;
import com.learning.globallearningcalendar.service.IBusinessUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessUnitServiceImpl implements IBusinessUnitService {

    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<BusinessUnitDTO> getAllBusinessUnits() {
        return businessUnitRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BusinessUnitDTO> getActiveBusinessUnits() {
        return businessUnitRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BusinessUnitDTO getBusinessUnitById(Long id) {
        BusinessUnit bu = businessUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));
        return toDTO(bu);
    }

    @Override
    public BusinessUnitDTO getBusinessUnitByCode(String code) {
        BusinessUnit bu = businessUnitRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "code", code));
        return toDTO(bu);
    }

    @Override
    public BusinessUnitDTO createBusinessUnit(BusinessUnitDTO dto) {
        BusinessUnit bu = toEntity(dto);
        bu.setIsActive(true);
        
        if (dto.getParentBusinessUnitId() != null) {
            BusinessUnit parent = businessUnitRepository.findById(dto.getParentBusinessUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent BusinessUnit", "id", dto.getParentBusinessUnitId()));
            bu.setParentBusinessUnit(parent);
        }
        
        BusinessUnit saved = businessUnitRepository.save(bu);
        return toDTO(saved);
    }

    @Override
    public BusinessUnitDTO updateBusinessUnit(Long id, BusinessUnitDTO dto) {
        BusinessUnit bu = businessUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));
        
        bu.setCode(dto.getCode());
        bu.setName(dto.getName());
        bu.setDescription(dto.getDescription());
        
        if (dto.getParentBusinessUnitId() != null) {
            BusinessUnit parent = businessUnitRepository.findById(dto.getParentBusinessUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent BusinessUnit", "id", dto.getParentBusinessUnitId()));
            bu.setParentBusinessUnit(parent);
        } else {
            bu.setParentBusinessUnit(null);
        }
        
        BusinessUnit updated = businessUnitRepository.save(bu);
        return toDTO(updated);
    }

    @Override
    public void deleteBusinessUnit(Long id) {
        BusinessUnit bu = businessUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));
        bu.setIsActive(false);
        businessUnitRepository.save(bu);
    }

    @Override
    public List<BusinessUnitDTO> getTopLevelBusinessUnits() {
        return businessUnitRepository.findTopLevelBusinessUnits().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BusinessUnitDTO> getChildBusinessUnits(Long parentId) {
        return businessUnitRepository.findChildBusinessUnits(parentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countUsersByBusinessUnit(Long buId) {
        return businessUnitRepository.countActiveUsersByBusinessUnit(buId);
    }

    private BusinessUnitDTO toDTO(BusinessUnit bu) {
        BusinessUnitDTO dto = BusinessUnitDTO.builder()
                .id(bu.getId())
                .code(bu.getCode())
                .name(bu.getName())
                .description(bu.getDescription())
                .isActive(bu.getIsActive())
                .createdAt(bu.getCreatedAt())
                .updatedAt(bu.getUpdatedAt())
                .build();
        
        if (bu.getParentBusinessUnit() != null) {
            dto.setParentBusinessUnitId(bu.getParentBusinessUnit().getId());
            dto.setParentBusinessUnitName(bu.getParentBusinessUnit().getName());
        }
        
        return dto;
    }

    private BusinessUnit toEntity(BusinessUnitDTO dto) {
        return BusinessUnit.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
