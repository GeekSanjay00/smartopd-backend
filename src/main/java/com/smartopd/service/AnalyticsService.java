package com.smartopd.service;

import com.smartopd.enums.TokenStatus;
import com.smartopd.model.Department;
import com.smartopd.repository.DepartmentRepository;
import com.smartopd.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TokenRepository tokenRepository;
    private final DepartmentRepository departmentRepository;

    // Get avg wait time per department
    public Map<String, Integer> getAvgWaitTimeByDepartment() {
        Map<String, Integer> result = new HashMap<>();

        departmentRepository.findAll().forEach(dept -> {
            result.put(dept.getName(), dept.getAvgWaitMinutes());
        });

        log.info("Avg wait time fetched for all departments");
        return result;
    }

    // Get total tokens today per department
    public Map<String, Integer> getTodayTokensByDepartment() {
        Map<String, Integer> result = new HashMap<>();
        LocalDateTime startOfDay = LocalDateTime.now()
                .with(LocalTime.MIDNIGHT);

        departmentRepository.findAll().forEach(dept -> {
            int count = tokenRepository
                    .countTodayTokensByDepartment(dept, startOfDay);
            result.put(dept.getName(), count);
        });

        return result;
    }

    // Get peak hours - tokens per hour today
    public Map<Integer, Long> getPeakHours() {
        Map<Integer, Long> hourlyCount = new HashMap<>();

        // Initialize all hours to 0
        for (int i = 0; i < 24; i++) {
            hourlyCount.put(i, 0L);
        }

        // Count tokens per hour
        LocalDateTime startOfDay = LocalDateTime.now()
                .with(LocalTime.MIDNIGHT);

        tokenRepository.findAll().stream()
                .filter(token -> token.getBookedAt() != null &&
                        token.getBookedAt().isAfter(startOfDay))
                .forEach(token -> {
                    int hour = token.getBookedAt().getHour();
                    hourlyCount.merge(hour, 1L, Long::sum);
                });

        return hourlyCount;
    }

    // Get overall stats
    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalTokensToday = tokenRepository.findAll().stream()
                .filter(t -> t.getBookedAt() != null &&
                        t.getBookedAt().isAfter(
                                LocalDateTime.now().with(LocalTime.MIDNIGHT)))
                .count();

        long totalDone = tokenRepository.findAll().stream()
                .filter(t -> t.getStatus() == TokenStatus.DONE)
                .filter(t -> t.getBookedAt() != null &&
                        t.getBookedAt().isAfter(
                                LocalDateTime.now().with(LocalTime.MIDNIGHT)))
                .count();

        long totalWaiting = tokenRepository.findAll().stream()
                .filter(t -> t.getStatus() == TokenStatus.WAITING)
                .count();

        List<Department> departments = departmentRepository.findAll();
        double avgWait = departments.stream()
                .mapToInt(Department::getAvgWaitMinutes)
                .average()
                .orElse(0);

        stats.put("totalTokensToday", totalTokensToday);
        stats.put("totalDone", totalDone);
        stats.put("totalWaiting", totalWaiting);
        stats.put("avgWaitMinutes", avgWait);
        stats.put("totalDepartments", departments.size());

        return stats;
    }
}
