package com.smartopd.service;

import com.smartopd.enums.TokenStatus;
import com.smartopd.exception.ResourceNotFoundException;
import com.smartopd.model.Department;
import com.smartopd.model.Doctor;
import com.smartopd.model.Token;
import com.smartopd.model.User;
import com.smartopd.repository.DepartmentRepository;
import com.smartopd.repository.DoctorRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TokenRepository tokenRepository;
    private final QueueService queueService;
    private final NotificationService notificationService;

    // Get all doctors
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Get doctor by id
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    // Get doctors by department
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found"));
        return doctorRepository.findByDepartment(department);
    }

    // Toggle availability
    public Doctor toggleAvailability(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        doctor.setAvailable(!doctor.isAvailable());
        doctorRepository.save(doctor);
        log.info("Doctor {} availability: {}", doctorId, doctor.isAvailable());
        return doctor;
    }

    // Call next patient
    public String callNextPatient(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        Long departmentId = doctor.getDepartment().getId();

        // Get next token from Redis queue
        String nextToken = queueService.getNextToken(departmentId);

        if (nextToken == null) {
            return "No patients in queue";
        }

        // Update token status to SERVING
        tokenRepository.findByTokenNumber(nextToken)
                .ifPresent(token -> {
                    token.setStatus(TokenStatus.SERVING);
                    tokenRepository.save(token);

                    // Send alert to next patient
                    notificationService.sendQueueAlert(
                            token.getUser().getPhone(),
                            nextToken, 1);

                    log.info("Calling patient with token: {}", nextToken);
                });

        return nextToken;
    }

    // Mark current token as done
    public void markTokenDone(Long doctorId, String tokenNumber) {
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Token not found"));

        token.setStatus(TokenStatus.DONE);
        token.setServedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // Check if next patient needs alert (position 3)
        Long departmentId = token.getDepartment().getId();
        List<String> queue = queueService
                .getQueueStatus(departmentId)
                .getWaitingTokens()
                .stream()
                .map(t -> t.getTokenNumber())
                .toList();

        if (queue.size() >= 3) {
            String thirdToken = queue.get(2);
            tokenRepository.findByTokenNumber(thirdToken)
                    .ifPresent(t -> notificationService.sendQueueAlert(
                            t.getUser().getPhone(), thirdToken, 3));
        }

        log.info("Token {} marked as done", tokenNumber);
    }

    // Skip current patient
    public void skipPatient(Long doctorId, String tokenNumber) {
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Token not found"));

        token.setStatus(TokenStatus.SKIPPED);
        tokenRepository.save(token);

        // Move to end of queue
        queueService.skipToken(
                token.getDepartment().getId(), tokenNumber);

        log.info("Token {} skipped", tokenNumber);
    }
}
