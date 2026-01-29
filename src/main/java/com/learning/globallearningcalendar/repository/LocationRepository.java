package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByName(String name);

    List<Location> findByCity(String city);

    List<Location> findByCountry(String country);

    List<Location> findByRegion(String region);

    List<Location> findByIsActiveTrue();

    @Query("SELECT l FROM Location l WHERE l.country = :country AND l.isActive = true")
    List<Location> findActiveLocationsByCountry(@Param("country") String country);

    @Query("SELECT DISTINCT l.country FROM Location l WHERE l.isActive = true ORDER BY l.country")
    List<String> findAllActiveCountries();

    @Query("SELECT DISTINCT l.region FROM Location l WHERE l.isActive = true AND l.region IS NOT NULL ORDER BY l.region")
    List<String> findAllActiveRegions();

    @Query("SELECT l FROM Location l WHERE l.capacity >= :minCapacity AND l.isActive = true")
    List<Location> findByMinCapacity(@Param("minCapacity") Integer minCapacity);
}
