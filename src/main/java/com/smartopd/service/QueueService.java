package com.smartopd.service;

import com.smartopd.dto.response.QueueStatusResponse;
import com.smartopd.exception.ResourceNotFoundException;
import com.smartopd.model.Department;
import com.smartopd.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DepartmentRepository departmentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    private static final String QUEUE_PREFIX = "queue:";

    // Add token to queue
    public void addToQueue(Long departmentId, String tokenNumber) {
        String key = QUEUE_PREFIX + departmentId;
        redisTemplate.opsForList().rightPush(key, tokenNumber);
        log.info("Token {} added to queue {}", tokenNumber, departmentId);
        broadcastQueueUpdate(departmentId);
    }

    // Get next token from queue
    public String getNextToken(Long departmentId) {
        String key = QUEUE_PREFIX + departmentId;
        String token = redisTemplate.opsForList().leftPop(key);
        log.info("Next token from queue {}: {}", departmentId, token);
        broadcastQueueUpdate(departmentId);
        return token;
    }

    // Skip token - move to end of queue
    public void skipToken(Long departmentId, String tokenNumber) {
        removeFromQueue(departmentId, tokenNumber);
        addToQueue(departmentId, tokenNumber);
        log.info("Token {} skipped in queue {}", tokenNumber, departmentId);
    }

    // Remove token from queue
    public void removeFromQueue(Long departmentId, String tokenNumber) {
        String key = QUEUE_PREFIX + departmentId;
        redisTemplate.opsForList().remove(key, 1, tokenNumber);
        log.info("Token {} removed from queue {}", tokenNumber, departmentId);
        broadcastQueueUpdate(departmentId);
    }

    // Get queue size
    public int getQueueSize(Long departmentId) {
        String key = QUEUE_PREFIX + departmentId;
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size.intValue() : 0;
    }

    // Get live queue status
    public QueueStatusResponse getQueueStatus(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Department not found"));

        String key = QUEUE_PREFIX + departmentId;
        List<String> tokens = redisTemplate.opsForList().range(key, 0, -1);

        List<QueueStatusResponse.TokenInfo> tokenInfoList = new ArrayList<>();
        if (tokens != null) {
            for (int i = 0; i < tokens.size(); i++) {
                tokenInfoList.add(QueueStatusResponse.TokenInfo.builder()
                        .tokenNumber(tokens.get(i))
                        .position(i + 1)
                        .estimatedWait("~" + ((i + 1) *
                                department.getAvgWaitMinutes()) + " min")
                        .build());
            }
        }

        return QueueStatusResponse.builder()
                .departmentId(departmentId)
                .departmentName(department.getName())
                .currentServingToken(department.getCurrentServing() > 0 ?
                        "MQ-" + department.getCurrentServing() : "None")
                .totalWaiting(tokens != null ? tokens.size() : 0)
                .avgWaitMinutes(department.getAvgWaitMinutes())
                .waitingTokens(tokenInfoList)
                .build();
    }

    // Broadcast queue update via WebSocket
    private void broadcastQueueUpdate(Long departmentId) {
        QueueStatusResponse status = getQueueStatus(departmentId);
        messagingTemplate.convertAndSend(
                "/topic/queue/" + departmentId, status);
        log.info("Queue update broadcasted for department: {}", departmentId);
    }
}
