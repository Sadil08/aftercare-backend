package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.CemeteryBooking;
import com.aftercare.aftercare_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CemeteryBookingRepository extends JpaRepository<CemeteryBooking, Long> {
    List<CemeteryBooking> findByCemeteryOwner(User owner);
    List<CemeteryBooking> findByFamilyMember(User familyMember);
    List<CemeteryBooking> findByCemeteryOwnerId(Long ownerId);
}
