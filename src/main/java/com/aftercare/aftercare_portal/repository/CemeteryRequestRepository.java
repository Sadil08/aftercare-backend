package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.CemeteryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CemeteryRequestRepository extends JpaRepository<CemeteryRequest, Long> {
    List<CemeteryRequest> findByFamilyNicNoOrderByCreatedAtDesc(String familyNicNo);
    List<CemeteryRequest> findByCemeteryUsernameOrderByCreatedAtDesc(String cemeteryUsername);
    List<CemeteryRequest> findByCemeteryUsernameAndRequestedDateAndStatusIn(String cemeteryUsername, String requestedDate, List<String> statuses);
    boolean existsByCr02FormId(Long cr02FormId);
}
