package com.smartopd.service;

import com.smartopd.enums.Role;
import com.smartopd.exception.ResourceNotFoundException;
import com.smartopd.model.Department;
import com.smartopd.model.Doctor;
import com.smartopd.model.User;
import com.smartopd.repository.DepartmentRepository;
import com.smartopd.repository.DoctorRepository;
import com.smartopd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Create department
    public Department createDepartment(String name, String description) {
        if (departmentRepository.existsByName(name)) {
            throw new RuntimeException("Department already exists: " + name);
        }

        Department department = Department.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .avgWaitMinutes(10)
                .build();

        departmentRepository.save(department);
        log.info("Department created: {}", name);
        return department;
    }

    // Get all departments
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Create doctor account
    public Doctor createDoctor(String name, String specialization,
                               Long departmentId, String phone,
                               String password) {
        // Check if phone already exists
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone already registered: " + phone);
        }

        // Find department
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found"));

        // Create user account for doctor
        User user = User.builder()
                .name(name)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(Role.DOCTOR)
                .isActive(true)
                .build();
        userRepository.save(user);

        // Create doctor profile
        Doctor doctor = Doctor.builder()
                .name(name)
                .specialization(specialization)
                .department(department)
                .user(user)
                .isAvailable(false)
                .build();

        doctorRepository.save(doctor);
        log.info("Doctor created: {}", name);
        return doctor;
    }

    // Deactivate user
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", userId);
    }

    // Activate user
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated: {}", userId);
    }
}
