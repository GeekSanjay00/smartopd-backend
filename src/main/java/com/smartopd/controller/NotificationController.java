package com.smartopd.controller;

import com.smartopd.dto.response.ApiResponse;
import com.smartopd.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Test SMS - only admin can do this
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<?>> testSms(
            @RequestParam String phone,
            @RequestParam String message) {
        notificationService.sendSms(phone, message);
        return ResponseEntity.ok(
                ApiResponse.success("Test SMS sent! Check console."));
    }

    // Send token confirmation manually
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/token-confirmation")
    public ResponseEntity<ApiResponse<?>> sendTokenConfirmation(
            @RequestParam String phone,
            @RequestParam String tokenNumber,
            @RequestParam String department,
            @RequestParam String waitTime) {
        notificationService.sendTokenConfirmation(
                phone, tokenNumber, department, waitTime);
        return ResponseEntity.ok(
                ApiResponse.success("Token confirmation sent!"));
    }
}