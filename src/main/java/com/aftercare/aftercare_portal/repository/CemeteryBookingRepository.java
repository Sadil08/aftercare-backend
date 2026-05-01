package com.aftercare.aftercare_portal.repository;

import com.aftercare.aftercare_portal.entity.CemeteryBooking;
import com.aftercare.aftercare_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CemeteryBookingRepository extends JpaRepository<CemeteryBooking, Long> {
    List<CemeteryBooking> findByCemeteryOwner(User owner);
    List<CemeteryBooking> findByFamilyMember(User familyMember);
    List<CemeteryBooking> findByCemeteryOwnerId(Long ownerId);
    Optional<CemeteryBooking> findFirstByDeathCaseIdOrderByIdDesc(Long deathCaseId);
    Optional<CemeteryBooking> findFirstByDeathCaseIdAndFamilyMemberIdOrderByIdDesc(Long deathCaseId, Long familyMemberId);
    boolean existsByDeathCaseIdAndFamilyMemberIdAndStatusIn(Long deathCaseId, Long familyMemberId, List<CemeteryBooking.BookingStatus> statuses);

    void deleteByDeathCaseId(Long deathCaseId);
}
