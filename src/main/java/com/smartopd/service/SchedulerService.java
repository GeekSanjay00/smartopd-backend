package com.smartopd.service;

import com.smartopd.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DepartmentRepository departmentRepository;

    // Reset all queues every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyQueues() {
        log.info("Daily queue reset started...");

        // Get all departments
        departmentRepository.findAll().forEach(department -> {
            String key = "queue:" + department.getId();

            // Clear Redis queue
            redisTemplate.delete(key);

            // Reset department stats
            department.setTotalTokensToday(0);
            department.setCurrentServing(0);
            departmentRepository.save(department);

            log.info("Queue reset for department: {}", department.getName());
        });

        log.info("Daily queue reset completed!");
    }

    // Update avg wait time every 30 minutes
    @Scheduled(fixedRate = 1800000)
    public void updateAvgWaitTime() {
        departmentRepository.findAll().forEach(department -> {
            // Simple calculation - can be improved
            int queueSize = (int) (redisTemplate.opsForList()
                    .size("queue:" + department.getId()) != null ?
                    redisTemplate.opsForList()
                            .size("queue:" + department.getId()) : 0);

            // Avg wait = queue size * 5 minutes per patient
            department.setAvgWaitMinutes(queueSize * 5);
            departmentRepository.save(department);
        });

        log.info("Avg wait time updated for all departments");
    }
}
