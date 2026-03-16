package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.B24Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface B24FormRepository extends JpaRepository<B24Form, Long> {
    long countByCurrentStage(String currentStage);
    long countByCurrentStageAndFamilyNicNo(String currentStage, String familyNicNo);
    long countByCurrentStageAndAssignedRegistrarUsername(String currentStage, String assignedRegistrarUsername);
    List<B24Form> findByFamilyNicNo(String familyNicNo);
    List<B24Form> findByAssignedRegistrarUsernameAndCurrentStage(String assignedRegistrarUsername, String currentStage);
}
