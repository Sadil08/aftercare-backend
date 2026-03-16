package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.B24Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface B24FormRepository extends JpaRepository<B24Form, Long> {
    long countByCurrentStage(String currentStage);
    long countByCurrentStageAndFamilyUserId(String currentStage, Long familyUserId);
    List<B24Form> findByFamilyUserId(Long familyUserId);
}
