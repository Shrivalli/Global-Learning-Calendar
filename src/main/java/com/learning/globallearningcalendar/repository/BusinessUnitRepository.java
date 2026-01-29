package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.BusinessUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, Long> {

    Optional<BusinessUnit> findByCode(String code);

    Optional<BusinessUnit> findByName(String name);

    List<BusinessUnit> findByIsActiveTrue();

    @Query("SELECT bu FROM BusinessUnit bu WHERE bu.parentBusinessUnit IS NULL AND bu.isActive = true")
    List<BusinessUnit> findTopLevelBusinessUnits();

    @Query("SELECT bu FROM BusinessUnit bu WHERE bu.parentBusinessUnit.id = :parentId AND bu.isActive = true")
    List<BusinessUnit> findChildBusinessUnits(@Param("parentId") Long parentId);

    @Query("SELECT bu FROM BusinessUnit bu LEFT JOIN FETCH bu.childBusinessUnits WHERE bu.id = :id")
    Optional<BusinessUnit> findByIdWithChildren(@Param("id") Long id);

    @Query("SELECT COUNT(u) FROM User u WHERE u.businessUnit.id = :buId AND u.isActive = true")
    Long countActiveUsersByBusinessUnit(@Param("buId") Long buId);
}
