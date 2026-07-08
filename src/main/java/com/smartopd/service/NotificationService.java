package com.smartopd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Async
    public void sendSms(String phone, String message) {
        // Development mode - print to console
        // Production mode - use Twilio SDK
        log.info("SMS to: {} | Message: {}", phone, message);
        System.out.println("SMS to: " + phone + " | " + message);
    }

    @Async
    public void sendTokenConfirmation(String phone, String tokenNumber,
                                      String department, String waitTime) {
        String message = String.format(
                "SmartOPD: Your token %s is confirmed for %s. " +
                        "Estimated wait: %s", tokenNumber, department, waitTime);
        sendSms(phone, message);
    }

    @Async
    public void sendQueueAlert(String phone, String tokenNumber, int position) {
        String message = String.format(
                "SmartOPD: Your token %s is at position %d. " +
                        "Please reach the hospital now!", tokenNumber, position);
        sendSms(phone, message);
    }

    @Async
    public void sendOtp(String phone, String otp) {
        String message = String.format(
                "SmartOPD: Your OTP is %s. " +
                        "Valid for 5 minutes. Do not share.", otp);
        sendSms(phone, message);
    }
}
