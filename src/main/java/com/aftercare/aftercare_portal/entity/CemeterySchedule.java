package com.aftercare.aftercare_portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cemetery_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CemeterySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cemeteryUsername;

    @Column(nullable = false)
    private String timeSlot; // e.g. "09:00 AM - 10:00 AM"
}
