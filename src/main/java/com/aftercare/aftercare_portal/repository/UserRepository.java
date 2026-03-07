package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNic(String nic);

    boolean existsByNic(String nic);
}
