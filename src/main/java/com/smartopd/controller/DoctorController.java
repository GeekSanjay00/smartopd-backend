package com.smartopd.controller;

import com.smartopd.dto.response.ApiResponse;
import com.smartopd.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // Get all doctors
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllDoctors() {
        var doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(
                ApiResponse.success("Doctors fetched successfully!", doctors));
    }

    // Get doctor by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getDoctorById(
            @PathVariable Long id) {
        var doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Doctor fetched successfully!", doctor));
    }

    // Get doctors by department
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<?>> getDoctorsByDepartment(
            @PathVariable Long departmentId) {
        var doctors = doctorService.getDoctorsByDepartment(departmentId);
        return ResponseEntity.ok(
                ApiResponse.success("Doctors fetched successfully!", doctors));
    }

    // Toggle availability - only doctor can do this
    @PreAuthorize("hasRole('DOCTOR')")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<?>> toggleAvailability(
            @PathVariable Long id) {
        var doctor = doctorService.toggleAvailability(id);
        return ResponseEntity.ok(
                ApiResponse.success("Availability updated!", doctor));
    }

    // Call next patient - only doctor can do this
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{id}/next")
    public ResponseEntity<ApiResponse<?>> callNextPatient(
            @PathVariable Long id) {
        String nextToken = doctorService.callNextPatient(id);
        return ResponseEntity.ok(
                ApiResponse.success("Next patient called!", nextToken));
    }

    // Mark token as done - only doctor can do this
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{id}/done/{tokenNumber}")
    public ResponseEntity<ApiResponse<?>> markTokenDone(
            @PathVariable Long id,
            @PathVariable String tokenNumber) {
        doctorService.markTokenDone(id, tokenNumber);
        return ResponseEntity.ok(
                ApiResponse.success("Token marked as done!"));
    }

    // Skip patient - only doctor can do this
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{id}/skip/{tokenNumber}")
    public ResponseEntity<ApiResponse<?>> skipPatient(
            @PathVariable Long id,
            @PathVariable String tokenNumber) {
        doctorService.skipPatient(id, tokenNumber);
        return ResponseEntity.ok(
                ApiResponse.success("Patient skipped!"));
    }
}