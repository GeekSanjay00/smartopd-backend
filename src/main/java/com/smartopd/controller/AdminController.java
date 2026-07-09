package com.smartopd.controller;

import com.smartopd.dto.response.ApiResponse;
import com.smartopd.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        var users = adminService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully!", users));
    }

    // Get all departments
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<?>> getAllDepartments() {
        var departments = adminService.getAllDepartments();
        return ResponseEntity.ok(
                ApiResponse.success("Departments fetched!", departments));
    }

    // Create department
    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<?>> createDepartment(
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        var department = adminService.createDepartment(name, description);
        return ResponseEntity.ok(
                ApiResponse.success("Department created successfully!", department));
    }

    // Create doctor
    @PostMapping("/doctors")
    public ResponseEntity<ApiResponse<?>> createDoctor(
            @RequestParam String name,
            @RequestParam String specialization,
            @RequestParam Long departmentId,
            @RequestParam String phone,
            @RequestParam String password) {
        var doctor = adminService.createDoctor(
                name, specialization, departmentId, phone, password);
        return ResponseEntity.ok(
                ApiResponse.success("Doctor created successfully!", doctor));
    }

    // Deactivate user
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<?>> deactivateUser(
            @PathVariable Long userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User deactivated successfully!"));
    }

    // Activate user
    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<ApiResponse<?>> activateUser(
            @PathVariable Long userId) {
        adminService.activateUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User activated successfully!"));
    }
}