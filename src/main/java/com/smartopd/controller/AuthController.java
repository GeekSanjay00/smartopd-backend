package com.smartopd.controller;

import com.smartopd.dto.request.LoginRequest;
import com.smartopd.dto.request.OtpVerifyRequest;
import com.smartopd.dto.request.RegisterRequest;
import com.smartopd.dto.response.ApiResponse;
import com.smartopd.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register new patient
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(
                ApiResponse.success("Registration successful! OTP sent to your phone."));
    }

    // Send OTP
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(
            @RequestParam String phone) {
        authService.sendOtp(phone);
        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully!"));
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request.getPhone(), request.getOtp());
        return ResponseEntity.ok(
                ApiResponse.success("OTP verified successfully!"));
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest request) {
        var response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful!", response));
    }
}
