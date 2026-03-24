package com.aftercare.aftercare_portal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
public class CemeterySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cemetery_owner_id", nullable = false)
    private User cemeteryOwner;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private boolean isAvailable = true;
}
