package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, String> {

    Optional<Citizen> findByNic(String nic);

    boolean existsByNic(String nic);
}
