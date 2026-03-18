package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.CemeterySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CemeteryScheduleRepository extends JpaRepository<CemeterySchedule, Long> {
    List<CemeterySchedule> findByCemeteryUsername(String cemeteryUsername);
}
