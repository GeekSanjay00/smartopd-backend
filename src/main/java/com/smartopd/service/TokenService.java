package com.smartopd.service;

import com.smartopd.dto.request.TokenBookRequest;
import com.smartopd.dto.response.TokenResponse;
import com.smartopd.enums.TokenStatus;
import com.smartopd.exception.DuplicateBookingException;
import com.smartopd.exception.ResourceNotFoundException;
import com.smartopd.model.Department;
import com.smartopd.model.Token;
import com.smartopd.model.User;
import com.smartopd.repository.DepartmentRepository;
import com.smartopd.repository.TokenRepository;
import com.smartopd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final QueueService queueService;
    private final NotificationService notificationService;

    // Book a token
    public TokenResponse bookToken(String phone, TokenBookRequest request) {

        // Find user
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Find department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found"));

        // Check duplicate booking
        boolean alreadyBooked = tokenRepository.existsByUserAndStatusIn(
                user, List.of(TokenStatus.WAITING, TokenStatus.SERVING));

        if (alreadyBooked) {
            throw new DuplicateBookingException(
                    "You already have an active token");
        }

        // Generate token number
        String tokenNumber = generateTokenNumber(department.getName());

        // Get current position in queue
        int position = queueService.getQueueSize(department.getId()) + 1;

        // Calculate estimated wait time
        String estimatedWait = "~" + (position * department.getAvgWaitMinutes()) + " min";

        // Create and save token
        Token token = Token.builder()
                .tokenNumber(tokenNumber)
                .user(user)
                .department(department)
                .status(TokenStatus.WAITING)
                .position(position)
                .build();

        tokenRepository.save(token);

        // Add to Redis queue
        queueService.addToQueue(department.getId(), tokenNumber);

        // Send confirmation SMS
        notificationService.sendTokenConfirmation(
                phone, tokenNumber, department.getName(), estimatedWait);

        log.info("Token booked: {} for user: {}", tokenNumber, phone);

        return TokenResponse.builder()
                .id(token.getId())
                .tokenNumber(tokenNumber)
                .departmentName(department.getName())
                .status(TokenStatus.WAITING)
                .position(position)
                .estimatedWait(estimatedWait)
                .bookedAt(LocalDateTime.now())
                .build();
    }

    // Cancel token
    public void cancelToken(Long tokenId, String phone) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Token not found"));

        // Check if token belongs to this user
        if (!token.getUser().getPhone().equals(phone)) {
            throw new RuntimeException("Unauthorized to cancel this token");
        }

        // Only WAITING tokens can be cancelled
        if (token.getStatus() != TokenStatus.WAITING) {
            throw new RuntimeException("Only waiting tokens can be cancelled");
        }

        token.setStatus(TokenStatus.CANCELLED);
        tokenRepository.save(token);

        // Remove from Redis queue
        queueService.removeFromQueue(
                token.getDepartment().getId(), token.getTokenNumber());

        log.info("Token cancelled: {}", tokenId);
    }

    // Get my tokens
    public List<Token> getMyTokens(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        return tokenRepository.findByUserOrderByBookedAtDesc(user);
    }

    // Generate unique token number
    private String generateTokenNumber(String departmentName) {
        String prefix = "MQ-" + departmentName.substring(0, 3).toUpperCase();
        int count = (int) tokenRepository.count() + 1;
        return String.format("%s-%03d", prefix, count);
    }
}