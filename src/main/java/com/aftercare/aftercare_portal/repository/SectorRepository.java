package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {
    Optional<Sector> findByCode(String code);
}
