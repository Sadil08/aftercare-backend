package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNicNo(String nicNo);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    boolean existsByNicNo(String nicNo);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    /** Lookup a doctor by their alphanumeric Doctor ID (e.g. DOC-A1B2C3). */
    Optional<User> findByDoctorId(String doctorId);
}
