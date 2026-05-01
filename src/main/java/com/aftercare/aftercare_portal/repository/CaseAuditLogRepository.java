package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.CaseAuditLog;
import com.aftercare.aftercare_portal.entity.DeathCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaseAuditLogRepository extends JpaRepository<CaseAuditLog, Long> {

    List<CaseAuditLog> findByDeathCaseOrderByPerformedAtAsc(DeathCase deathCase);

    @Query("SELECT COUNT(l) FROM CaseAuditLog l WHERE l.performedByUsername = :username AND l.action = :action AND l.performedAt > :since")
    long countByActorActionSince(
            @Param("username") String username,
            @Param("action") String action,
            @Param("since") LocalDateTime since);
}
