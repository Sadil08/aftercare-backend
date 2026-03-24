package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.entity.CemeteryBooking;
import com.aftercare.aftercare_portal.entity.CemeterySchedule;
import com.aftercare.aftercare_portal.entity.DeathCase;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.CemeteryBookingRepository;
import com.aftercare.aftercare_portal.repository.CemeteryScheduleRepository;
import com.aftercare.aftercare_portal.repository.DeathCaseRepository;
import com.aftercare.aftercare_portal.repository.UserRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CemeteryController {

    private final UserRepository userRepository;
    private final CemeteryScheduleRepository scheduleRepository;
    private final CemeteryBookingRepository bookingRepository;
    private final DeathCaseRepository deathCaseRepository;

    public CemeteryController(UserRepository userRepository,
                              CemeteryScheduleRepository scheduleRepository,
                              CemeteryBookingRepository bookingRepository,
                              DeathCaseRepository deathCaseRepository) {
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.deathCaseRepository = deathCaseRepository;
    }

    // ─── USER APIS ───

    @GetMapping("/cemeteries")
    @PreAuthorize("hasRole('FAMILY')")
    public ResponseEntity<List<CemeteryDto>> getAvailableCemeteries() {
        List<User> cemeteries = userRepository.findByRole(Role.CEMETERY);
        List<CemeteryDto> dtos = cemeteries.stream()
                .map(c -> new CemeteryDto(c.getId(), c.getFullName(), c.getPhone(), c.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/cemeteries/{id}/schedule")
    @PreAuthorize("hasRole('FAMILY')")
    public ResponseEntity<List<CemeteryScheduleDto>> getCemeterySchedule(@PathVariable Long id) {
        List<CemeterySchedule> schedules = scheduleRepository.findByCemeteryOwnerId(id);
        List<CemeteryScheduleDto> dtos = schedules.stream()
                .map(s -> new CemeteryScheduleDto(s.getId(), s.getDate(), s.getStartTime(), s.getEndTime(), s.isAvailable()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/cemeteries/{id}/bookings")
    @PreAuthorize("hasRole('FAMILY')")
    public ResponseEntity<String> createBooking(@PathVariable("id") Long cemeteryId,
                                                @RequestBody CreateBookingRequest request,
                                                Authentication authentication) {
        User cemeteryOwner = userRepository.findById(cemeteryId).orElseThrow(() -> new IllegalArgumentException("Cemetery not found"));
        User familyMember = userRepository.findByUsername(authentication.getName()).orElseThrow();
        DeathCase deathCase = deathCaseRepository.findById(request.getDeathCaseId()).orElseThrow(() -> new IllegalArgumentException("Death case not found"));

        if (!deathCase.getApplicantFamilyMember().getId().equals(familyMember.getId())) {
            return ResponseEntity.status(403).body("Unauthorized to book for this case.");
        }

        CemeteryBooking booking = new CemeteryBooking();
        booking.setCemeteryOwner(cemeteryOwner);
        booking.setFamilyMember(familyMember);
        booking.setDeathCase(deathCase);
        booking.setBookingDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(CemeteryBooking.BookingStatus.PENDING);
        booking.setNotes(request.getNotes());

        bookingRepository.save(booking);
        return ResponseEntity.ok("Booking requested successfully.");
    }

    // ─── CEMETERY OWNER APIS ───

    @GetMapping("/cemetery-owner/bookings")
    @PreAuthorize("hasRole('CEMETERY')")
    public ResponseEntity<List<BookingDto>> getOwnerBookings(Authentication authentication) {
        User owner = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<CemeteryBooking> bookings = bookingRepository.findByCemeteryOwner(owner);

        List<BookingDto> dtos = bookings.stream().map(b -> new BookingDto(
                b.getId(),
                b.getDeathCase().getId(),
                b.getDeathCase().getDeceased().getFullName(),
                b.getBookingDate(),
                b.getStartTime(),
                b.getEndTime(),
                b.getStatus().name(),
                b.getNotes()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/cemetery-owner/schedule")
    @PreAuthorize("hasRole('CEMETERY')")
    public ResponseEntity<List<CemeteryScheduleDto>> getOwnerSchedules(Authentication authentication) {
        User owner = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<CemeterySchedule> schedules = scheduleRepository.findByCemeteryOwner(owner);

        List<CemeteryScheduleDto> dtos = schedules.stream().map(s -> new CemeteryScheduleDto(
                s.getId(), s.getDate(), s.getStartTime(), s.getEndTime(), s.isAvailable()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/cemetery-owner/schedule")
    @PreAuthorize("hasRole('CEMETERY')")
    public ResponseEntity<String> addOwnerSchedule(@RequestBody CreateScheduleRequest request,
                                                   Authentication authentication) {
        User owner = userRepository.findByUsername(authentication.getName()).orElseThrow();

        CemeterySchedule schedule = new CemeterySchedule();
        schedule.setCemeteryOwner(owner);
        schedule.setDate(request.getDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setAvailable(true);

        scheduleRepository.save(schedule);
        return ResponseEntity.ok("Schedule block added successfully.");
    }

    @PutMapping("/cemetery-owner/bookings/{id}")
    @PreAuthorize("hasRole('CEMETERY')")
    public ResponseEntity<String> updateBookingStatus(@PathVariable Long id,
                                                      @RequestBody UpdateBookingStatusRequest request,
                                                      Authentication authentication) {
        User owner = userRepository.findByUsername(authentication.getName()).orElseThrow();
        CemeteryBooking booking = bookingRepository.findById(id).orElseThrow();

        if (!booking.getCemeteryOwner().getId().equals(owner.getId())) {
            return ResponseEntity.status(403).body("Not your booking.");
        }

        booking.setStatus(CemeteryBooking.BookingStatus.valueOf(request.getStatus()));
        bookingRepository.save(booking);

        return ResponseEntity.ok("Booking status updated to " + request.getStatus());
    }

    // ─── DTOs ───

    @Data
    public static class CemeteryDto {
        private final Long id;
        private final String name;
        private final String phone;
        private final String email;
    }

    @Data
    public static class CemeteryScheduleDto {
        private final Long id;
        private final LocalDate date;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final boolean isAvailable;
    }

    @Data
    public static class CreateBookingRequest {
        private Long deathCaseId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String notes;
    }

    @Data
    public static class BookingDto {
        private final Long id;
        private final Long deathCaseId;
        private final String deceasedName;
        private final LocalDate date;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final String status;
        private final String notes;
    }

    @Data
    public static class CreateScheduleRequest {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Data
    public static class UpdateBookingStatusRequest {
        private String status; // APPROVED, REJECTED
    }
}
