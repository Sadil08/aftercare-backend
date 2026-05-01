package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.CaseAuditLog;
import com.aftercare.aftercare_portal.entity.DeathCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseAuditLogRepository extends JpaRepository<CaseAuditLog, Long> {

    List<CaseAuditLog> findByDeathCaseOrderByPerformedAtAsc(DeathCase deathCase);
}
