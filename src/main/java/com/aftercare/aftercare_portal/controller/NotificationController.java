package com.aftercare.aftercare_portal.controller;

import com.aftercare.aftercare_portal.dto.NotificationDTO;
import com.aftercare.aftercare_portal.service.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final FormService formService;

    @GetMapping("/unread")
    public ResponseEntity<NotificationDTO> getUnreadNotifications(
            @RequestParam Long userId, 
            @RequestParam String role) {
        
        NotificationDTO notifications = formService.getUnreadNotifications(userId, role);
        return ResponseEntity.ok(notifications);
    }
}
