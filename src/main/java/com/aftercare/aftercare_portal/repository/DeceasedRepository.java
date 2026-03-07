package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.Deceased;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeceasedRepository extends JpaRepository<Deceased, Long> {
    boolean existsByNic(String nic);
}
