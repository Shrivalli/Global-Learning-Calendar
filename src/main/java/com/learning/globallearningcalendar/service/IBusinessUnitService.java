package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.BusinessUnitDTO;

import java.util.List;

public interface IBusinessUnitService {

    List<BusinessUnitDTO> getAllBusinessUnits();

    List<BusinessUnitDTO> getActiveBusinessUnits();

    BusinessUnitDTO getBusinessUnitById(Long id);

    BusinessUnitDTO getBusinessUnitByCode(String code);

    BusinessUnitDTO createBusinessUnit(BusinessUnitDTO dto);

    BusinessUnitDTO updateBusinessUnit(Long id, BusinessUnitDTO dto);

    void deleteBusinessUnit(Long id);

    List<BusinessUnitDTO> getTopLevelBusinessUnits();

    List<BusinessUnitDTO> getChildBusinessUnits(Long parentId);

    Long countUsersByBusinessUnit(Long buId);
}
