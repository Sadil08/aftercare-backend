package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.DeathCase;
import com.aftercare.aftercare_portal.entity.Sector;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.DeathCaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeathCaseRepository extends JpaRepository<DeathCase, Long> {

    // For GN — cases in their sector with a specific status
    Page<DeathCase> findByStatusAndSector(DeathCaseStatus status, Sector sector, Pageable pageable);

    // For Citizen — their own cases
    Page<DeathCase> findByApplicantFamilyMember(User applicant, Pageable pageable);

    // For Registrar / status-based queries
    Page<DeathCase> findByStatus(DeathCaseStatus status, Pageable pageable);

    // For Doctor — cases assigned specifically to them
    Page<DeathCase> findByStatusAndAssignedDoctor(DeathCaseStatus status, User doctor, Pageable pageable);

    // Search by deceased NIC
    Optional<DeathCase> findByDeceased_Nic(String nic);

    // Search by applicant NIC (for auto-fill in FormService)
    Optional<DeathCase> findFirstByApplicantFamilyMember_NicNoOrderByCreatedAtDesc(String nicNo);

    // All cases in a sector (for GN dashboard)
    List<DeathCase> findBySector(Sector sector);

    // Certificate verification
    Optional<DeathCase> findByFormCr2_CertificateSerialNumber(String serialNumber);

    // Counts for Notifications
    long countByStatus(DeathCaseStatus status);
    long countByStatusAndSector(DeathCaseStatus status, Sector sector);
    long countByApplicantFamilyMemberAndStatus(User applicant, DeathCaseStatus status);
}

