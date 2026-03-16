package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.Cr02Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Cr02FormRepository extends JpaRepository<Cr02Form, Long> {
    long countByCurrentStage(String currentStage);
    long countByCurrentStageAndFamilyNicNo(String currentStage, String familyNicNo);
    List<Cr02Form> findByFamilyNicNo(String familyNicNo);
    List<Cr02Form> findBySubmittedByUsername(String submittedByUsername);
}
