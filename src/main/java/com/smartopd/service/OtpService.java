package com.smartopd.service;

import com.smartopd.exception.InvalidOtpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;

    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_EXPIRY_MINUTES = 5;

    // Generate and send OTP
    public void generateAndSendOtp(String phone) {
        // Generate 6 digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Store in Redis with 5 min expiry
        redisTemplate.opsForValue().set(
                OTP_PREFIX + phone,
                otp,
                OTP_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );

        // Send OTP via SMS
        notificationService.sendOtp(phone, otp);

        log.info("OTP generated for phone: {}", phone);
    }

    // Verify OTP
    public void verifyOtp(String phone, String otp) {
        String savedOtp = redisTemplate.opsForValue()
                .get(OTP_PREFIX + phone);

        // OTP not found or expired
        if (savedOtp == null) {
            throw new InvalidOtpException("OTP has expired. Please request a new one.");
        }

        // OTP does not match
        if (!savedOtp.equals(otp)) {
            throw new InvalidOtpException("Invalid OTP. Please try again.");
        }

        // Delete OTP after successful verification
        redisTemplate.delete(OTP_PREFIX + phone);

        log.info("OTP verified successfully for phone: {}", phone);
    }
}
