package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.LocationDTO;

import java.util.List;

public interface ILocationService {

    List<LocationDTO> getAllLocations();

    List<LocationDTO> getActiveLocations();

    LocationDTO getLocationById(Long id);

    LocationDTO createLocation(LocationDTO dto);

    LocationDTO updateLocation(Long id, LocationDTO dto);

    void deleteLocation(Long id);

    List<LocationDTO> getLocationsByCountry(String country);

    List<String> getAllCountries();

    List<String> getAllRegions();
}
