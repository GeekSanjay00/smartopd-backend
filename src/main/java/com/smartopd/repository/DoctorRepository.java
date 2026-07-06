package com.smartopd.repository;

import com.smartopd.model.Department;
import com.smartopd.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartment(Department department);

    List<Doctor> findByIsAvailableTrue();

    List<Doctor> findByDepartmentAndIsAvailableTrue(Department department);

    Optional<Doctor> findByUserId(Long userId);
}