package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.entity.CemeterySchedule;
import com.aftercare.aftercare_portal.repository.CemeteryScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cemetery-schedules")
@RequiredArgsConstructor
public class CemeteryScheduleController {

    private final CemeteryScheduleRepository repository;

    @GetMapping("/{username}")
    public ResponseEntity<List<CemeterySchedule>> getSchedules(@PathVariable String username) {
        return ResponseEntity.ok(repository.findByCemeteryUsername(username));
    }

    @PostMapping("/{username}")
    public ResponseEntity<CemeterySchedule> addSchedule(@PathVariable String username, @RequestBody CemeterySchedule schedule) {
        schedule.setCemeteryUsername(username);
        return ResponseEntity.ok(repository.save(schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
