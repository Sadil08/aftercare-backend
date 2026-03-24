package com.aftercare.aftercare_portal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
public class CemeteryBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cemetery_owner_id", nullable = false)
    private User cemeteryOwner;

    @ManyToOne
    @JoinColumn(name = "family_member_id", nullable = false)
    private User familyMember;

    @ManyToOne
    @JoinColumn(name = "death_case_id", nullable = false)
    private DeathCase deathCase;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String notes;

    public enum BookingStatus {
        PENDING, APPROVED, REJECTED
    }
}
