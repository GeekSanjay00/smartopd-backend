package com.smartopd.service;

import com.smartopd.dto.request.LoginRequest;
import com.smartopd.dto.request.RegisterRequest;
import com.smartopd.enums.Role;
import com.smartopd.exception.ResourceNotFoundException;
import com.smartopd.model.User;
import com.smartopd.repository.UserRepository;
import com.smartopd.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    // Register new patient
    public void register(RegisterRequest request) {
        // Check if phone already registered
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        // Create new user with isActive = true
        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PATIENT)
                .isActive(true)
                .build();

        userRepository.save(user);

        // Send OTP for verification
        otpService.generateAndSendOtp(request.getPhone());

        log.info("New patient registered: {}", request.getPhone());
    }

    // Login user
    public Map<String, String> login(LoginRequest request) {
        // Find user by phone
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getPhone(),
                user.getRole().name()
        );

        // Return token and user info
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole().name());
        response.put("name", user.getName());
        response.put("phone", user.getPhone());

        log.info("User logged in: {}", request.getPhone());
        return response;
    }

    // Send OTP
    public void sendOtp(String phone) {
        if (!userRepository.existsByPhone(phone)) {
            throw new ResourceNotFoundException("User not found with phone: " + phone);
        }
        otpService.generateAndSendOtp(phone);
    }

    // Verify OTP
    public void verifyOtp(String phone, String otp) {
        otpService.verifyOtp(phone, otp);
        log.info("OTP verified for: {}", phone);
    }
}